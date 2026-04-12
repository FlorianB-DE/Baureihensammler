/**
 * Prüft alle wikiArticleTitle aus TrainSeries.kt gegen die de.wikipedia REST Summary API.
 * Encoding wie in Kotlin: java.net.URLEncoder.encode(title, UTF_8).
 *
 * Ausführen (Repo-Root):
 *   deno run --allow-read=app --allow-net=de.wikipedia.org tools/check_wiki_summaries.ts
 */
const trainSeriesPath = new URL(
  "../app/src/main/java/eu/florianbecker/baureihensammler/data/TrainSeries.kt",
  import.meta.url,
);

/** Entspricht java.net.URLEncoder.encode(s, UTF_8) für typische Titel (UTF-8, + für Leerzeichen). */
function javaNetUrlEncoderEncode(s: string): string {
  const out: string[] = [];
  for (const ch of s) {
    if (ch === " ") {
      out.push("+");
    } else if (/^[a-zA-Z0-9*._\-]$/.test(ch)) {
      out.push(ch);
    } else {
      for (const b of new TextEncoder().encode(ch)) {
        out.push("%" + b.toString(16).toUpperCase().padStart(2, "0"));
      }
    }
  }
  return out.join("");
}

function extractTitles(text: string): Set<string> {
  const titles = new Set<string>();
  for (
    const m of text.matchAll(/wikiArticleTitle\s*=\s*"([^"]+)"/g)
  ) {
    titles.add(m[1]!);
  }
  const pos = new RegExp(
    String.raw`TrainSeries\s*\(\s*"[^"]*"\s*,\s*"[^"]*"\s*,\s*"[^"]*"\s*,\s*\d+\s*,\s*\d+\s*,\s*"([^"]+)"`,
    "g",
  );
  for (const m of text.matchAll(pos)) {
    titles.add(m[1]!);
  }
  return titles;
}

const UA =
  "BaureihensammlerWikiCheck/1.0 (Deno; https://de.wikipedia.org/wiki/Wikipedia:API)";

async function main() {
  const text = await Deno.readTextFile(trainSeriesPath);
  const titles = extractTitles(text);
  const sorted = [...titles].sort();

  console.log("code\tstored_title\tapi_title\textract_len\ttype");
  let non200 = 0;
  let emptyExtract = 0;

  for (const t of sorted) {
    const enc = javaNetUrlEncoderEncode(t);
    const url = `https://de.wikipedia.org/api/rest_v1/page/summary/${enc}`;
    let code = "???";
    let apiTitle = "";
    let extractLen = 0;
    let typ = "";
    try {
      const res = await fetch(url, {
        headers: { "Accept": "application/json; charset=utf-8", "User-Agent": UA },
      });
      code = String(res.status);
      if (res.ok) {
        const j = (await res.json()) as {
          title?: string;
          extract?: string;
          type?: string;
        };
        apiTitle = j.title ?? "";
        const ex = (j.extract ?? "").trim();
        extractLen = ex.length;
        typ = j.type ?? "";
        if (extractLen === 0) emptyExtract++;
      }
    } catch {
      code = "ERR";
    }
    if (code !== "200") non200++;
    console.log(
      `${code}\t${t}\t${apiTitle}\t${extractLen}\t${typ}`,
    );
    await new Promise((r) => setTimeout(r, 150));
  }

  console.error(`\nunique titles: ${sorted.length}`);
  console.error(`non-200: ${non200}`);
  console.error(`200 but empty extract: ${emptyExtract}`);
}

await main();
