import { detectMessageType, extractYoutubeId, renderTextWithLinks } from '../utils/linkParser';

function MessageContent({ content }) {
  const type = detectMessageType(content);

  if (type === 'youtube') {
    const videoId = extractYoutubeId(content);
    const textParts = renderTextWithLinks(content);

    return (
      <div>
        <p className="message-text">
          {textParts.map(part =>
            part.type === 'link'
              ? <a key={part.key} href={part.value} target="_blank" rel="noreferrer" className="message-link">{part.value}</a>
              : <span key={part.key}>{part.value}</span>
          )}
        </p>
        <div className="youtube-embed">
          <iframe
            width="280"
            height="157"
            src={`https://www.youtube.com/embed/${videoId}`}
            title="YouTube video"
            frameBorder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          />
        </div>
      </div>
    );
  }

  if (type === 'link') {
    const textParts = renderTextWithLinks(content);
    const urls = textParts.filter(p => p.type === 'link').map(p => p.value);

    return (
      <div>
        <p className="message-text">
          {textParts.map(part =>
            part.type === 'link'
              ? <a key={part.key} href={part.value} target="_blank" rel="noreferrer" className="message-link">{part.value}</a>
              : <span key={part.key}>{part.value}</span>
          )}
        </p>
        {urls.map((url, i) => (
          <LinkPreview key={i} url={url} />
        ))}
      </div>
    );
  }

  return <p className="message-text">{content}</p>;
}

function LinkPreview({ url }) {
  const domain = new URL(url).hostname.replace('www.', '');
  const isInstagram = domain.includes('instagram');
  const isTiktok = domain.includes('tiktok');
  const isTwitter = domain.includes('twitter') || domain.includes('x.com');
  const isSpotify = domain.includes('spotify');

  const getIcon = () => {
    if (isInstagram) return '📸';
    if (isTiktok) return '🎵';
    if (isTwitter) return '🐦';
    if (isSpotify) return '🎧';
    return '🔗';
  };

  const getName = () => {
    if (isInstagram) return 'Instagram';
    if (isTiktok) return 'TikTok';
    if (isTwitter) return 'Twitter / X';
    if (isSpotify) return 'Spotify';
    return domain;
  };

  return (
    <a href={url} target="_blank" rel="noreferrer" className="link-preview">
      <div className="link-preview-icon">{getIcon()}</div>
      <div className="link-preview-info">
        <p className="link-preview-name">{getName()}</p>
        <p className="link-preview-url">{url.length > 40 ? url.substring(0, 40) + '...' : url}</p>
      </div>
      <span className="link-preview-arrow">→</span>
    </a>
  );
}

export default MessageContent;