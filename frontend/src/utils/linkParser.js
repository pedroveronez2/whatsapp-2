export function detectMessageType(content) {
  if (!content) return 'text';

  const youtubeRegex = /(?:https?:\/\/)?(?:www\.)?(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/;
  const urlRegex = /(https?:\/\/[^\s]+)/g;

  if (youtubeRegex.test(content)) return 'youtube';
  if (urlRegex.test(content)) return 'link';
  return 'text';
}

export function extractYoutubeId(content) {
  const match = content.match(
    /(?:https?:\/\/)?(?:www\.)?(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/
  );
  return match ? match[1] : null;
}

export function extractUrls(content) {
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  return content.match(urlRegex) || [];
}

export function renderTextWithLinks(content) {
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  return content.split(urlRegex).map((part, i) => {
    if (urlRegex.test(part)) {
      urlRegex.lastIndex = 0;
      return { type: 'link', value: part, key: i };
    }
    return { type: 'text', value: part, key: i };
  });
}