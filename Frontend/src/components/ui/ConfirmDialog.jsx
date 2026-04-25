import Modal from './Modal'

export default function ConfirmDialog({ open, onClose, onConfirm, title, message, loading }) {
  return (
    <Modal open={open} onClose={onClose} title={title || 'Confirmer'}
      footer={
        <>
          <button className="btn btn-ghost" onClick={onClose} disabled={loading}>Annuler</button>
          <button className="btn btn-danger" onClick={onConfirm} disabled={loading}>
            {loading ? <span className="spinner" style={{ width:16,height:16 }} /> : null}
            Supprimer
          </button>
        </>
      }
    >
      <p style={{ color: 'var(--text-secondary)', lineHeight: 1.6 }}>{message || 'Cette action est irréversible.'}</p>
    </Modal>
  )
}
