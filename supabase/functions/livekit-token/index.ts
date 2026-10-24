// Supabase Edge Function: issue a short-lived LiveKit room token.
//
// Deploy:
//   supabase functions deploy livekit-token
//
// Required function secrets (set in Supabase, never in the Android app):
//   LIVEKIT_URL=wss://your-project.livekit.cloud
//   LIVEKIT_API_KEY=...
//   LIVEKIT_API_SECRET=...
//
// Supabase supplies SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY automatically.

import { createClient } from "npm:@supabase/supabase-js@2.118.0";
import { AccessToken, TrackSource } from "npm:livekit-server-sdk@2.19.1";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return json({ error: "Method not allowed" }, 405);

  try {
    const authorization = req.headers.get("Authorization");
    if (!authorization) return json({ error: "Missing authorization" }, 401);

    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
      { auth: { persistSession: false } },
    );

    const jwt = authorization.replace(/^Bearer\s+/i, "");
    const { data: authData, error: authError } = await supabase.auth.getUser(jwt);
    if (authError || !authData.user) return json({ error: "Invalid session" }, 401);

    const { session_id: sessionId } = await req.json().catch(() => ({}));
    if (typeof sessionId !== "string" || sessionId.length < 10) {
      return json({ error: "session_id is required" }, 400);
    }

    const { data: session, error: sessionError } = await supabase
      .from("call_sessions")
      .select("id, room_name, caller_id, callee_id, status")
      .eq("id", sessionId)
      .maybeSingle();

    if (sessionError || !session) return json({ error: "Call not found" }, 404);
    if (session.caller_id !== authData.user.id && session.callee_id !== authData.user.id) {
      return json({ error: "Not a participant in this call" }, 403);
    }
    if (session.status !== "active") {
      return json({ error: "Call is not active yet" }, 409);
    }

    const { data: profile, error: profileError } = await supabase
      .from("profiles")
      .select("display_name, is_banned")
      .eq("id", authData.user.id)
      .maybeSingle();

    if (profileError || !profile) return json({ error: "Profile not found" }, 404);
    if (profile.is_banned) return json({ error: "This account cannot join calls" }, 403);

    const url = Deno.env.get("LIVEKIT_URL");
    const apiKey = Deno.env.get("LIVEKIT_API_KEY");
    const apiSecret = Deno.env.get("LIVEKIT_API_SECRET");
    if (!url || !apiKey || !apiSecret) {
      return json({ error: "LiveKit is not configured" }, 500);
    }

    const at = new AccessToken(apiKey, apiSecret, {
      identity: authData.user.id,
      name: profile.display_name,
      ttl: "2h",
    });
    at.addGrant({
      roomJoin: true,
      room: session.room_name,
      canPublish: true,
      canPublishSources: [TrackSource.MICROPHONE],
      canPublishData: true,
      canSubscribe: true,
    });

    return json({ url, token: await at.toJwt() });
  } catch (error) {
    console.error("livekit-token error", error);
    return json({ error: "Could not create a call token" }, 500);
  }
});
