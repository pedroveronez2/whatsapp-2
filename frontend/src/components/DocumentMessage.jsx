function getDocumentIcon(fileName) {
  if (!fileName) return '📄';
  const ext = fileName.split('.').pop().toLowerCase();
  const icons = {
    pdf: '📕',
    doc: '📘', docx: '📘',
    xls: '📗', xlsx: '📗',
    ppt: '📙', pptx: '📙',
    txt: '📃',
  };
  return icons[ext] || '📄';
}

function getDocumentType(fileName) {
  if (!fileName) return 'Documento';
  const ext = fileName.split('.').pop().toLowerCase();
  const types = {
    pdf: 'PDF',
    doc: 'Word', docx: 'Word',
    xls: 'Excel', xlsx: 'Excel',
    ppt: 'PowerPoint', pptx: 'PowerPoint',
    txt: 'Texto',
  };
  return types[ext] || ext.toUpperCase();
}

function DocumentMessage({ mediaUrl, fileName }) {
  const fullUrl = `http://localhost:8080${mediaUrl}`;
  const icon = getDocumentIcon(fileName);
  const type = getDocumentType(fileName);
  const name = fileName || 'documento';

  async function handleDownload() {
    const resp = await fetch(fullUrl);
    const blob = await resp.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = name;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  return (
    <div className="document-message">
      <div className="document-icon">{icon}</div>
      <div className="document-info">
        <p className="document-name">{name}</p>
        <p className="document-type">{type}</p>
      </div>
      <button className="document-download" onClick={handleDownload} title="Baixar">
        ⬇️
      </button>
    </div>
  );
}

export default DocumentMessage;