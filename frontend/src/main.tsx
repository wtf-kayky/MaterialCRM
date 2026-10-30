import { useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import './styles.css'

type ServiceState = 'checking' | 'online' | 'offline'

function App() {
  const [serviceState, setServiceState] = useState<ServiceState>('checking')

  useEffect(() => {
    fetch('/api/actuator/health')
      .then((response) => {
        if (!response.ok) throw new Error('API indisponível')
        return response.json()
      })
      .then(() => setServiceState('online'))
      .catch(() => setServiceState('offline'))
  }, [])

  const statusLabel = {
    checking: 'Verificando API local',
    online: 'API local disponível',
    offline: 'API local indisponível',
  }[serviceState]

  return (
    <main className="shell">
      <section className="card" aria-labelledby="title">
        <span className={`status status--${serviceState}`} aria-live="polite">{statusLabel}</span>
        <p className="eyebrow">FUNDAÇÃO TÉCNICA</p>
        <h1 id="title">MaterialCRM</h1>
        <p className="description">
          Ambiente local preparado para a operação de lojas de materiais de construção.
        </p>
      </section>
    </main>
  )
}

createRoot(document.getElementById('root')!).render(<App />)

