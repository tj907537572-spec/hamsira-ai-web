export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      return res.status(500).json({ error: 'GEMINI_API_KEY is not configured on Vercel' });
    }

    const model = process.env.GEMINI_MODEL || 'gemini-flash-latest';
    const { prompt, contents, systemInstruction } = req.body || {};

    const body = contents
      ? { contents, ...(systemInstruction ? { systemInstruction } : {}) }
      : {
          contents: [
            {
              role: 'user',
              parts: [{ text: String(prompt || '') }],
            },
          ],
        };

    const response = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      }
    );

    const data = await response.json();

    if (!response.ok) {
      console.error('Gemini error', response.status, JSON.stringify(data));
    }

    return res.status(response.status).json(data);
  } catch (err) {
    console.error('Proxy error', err);
    return res.status(500).json({ error: err.message || 'Proxy error' });
  }
      }
