// Public post-verification page for Supabase auth emails.
// Supabase's function gateway forces text/plain for public functions, so keep
// this intentionally simple and readable instead of returning HTML.
Deno.serve(() => {
  const body = [
    "LINGO",
    "English, every day",
    "",
    "You're verified.",
    "",
    "Return to the Lingo app and sign in to start speaking.",
    "You can close this tab now.",
    "",
  ].join("\n");

  return new Response(body, {
    status: 200,
    headers: {
      "content-type": "text/plain; charset=utf-8",
      "cache-control": "no-store",
      "x-content-type-options": "nosniff",
      "referrer-policy": "no-referrer",
    },
  });
});
