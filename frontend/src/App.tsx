import { useState } from 'react'
import type { FormEvent } from 'react'
import {
  Bell,
  Buildings,
  CaretDown,
  ChartLineUp,
  ClipboardText,
  Cube,
  GearSix,
  House,
  List,
  MagnifyingGlass,
  SignOut,
  Storefront,
  UserCircle,
  UsersThree,
  X,
} from '@phosphor-icons/react'

type Screen = 'dashboard' | 'login'

const navigation = [
  { label: 'Visão geral', icon: House },
  { label: 'Vendas', icon: ChartLineUp },
  { label: 'Clientes', icon: UsersThree },
  { label: 'Produtos', icon: Cube },
  { label: 'Estoque', icon: ClipboardText },
]

const notifications = [
  { title: 'Estoque em revisão', detail: 'A API local está pronta para receber o módulo de estoque.', time: 'agora' },
  { title: 'Fundação técnica', detail: 'Serviços locais configurados para a primeira execução.', time: 'há 12 min' },
]

function App() {
  const [screen, setScreen] = useState<Screen>('dashboard')
  const [branchOpen, setBranchOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)

  if (screen === 'login') {
    return <Login onEnter={() => setScreen('dashboard')} />
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark">M</span><span>MaterialCRM</span></div>
        <nav className="sidebar-nav" aria-label="Navegação principal">
          {navigation.map(({ label, icon: Icon }, index) => (
            <button className={`nav-item ${index === 0 ? 'nav-item--active' : ''}`} key={label} type="button">
              <Icon size={19} weight="regular" />
              <span>{label}</span>
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <button className="nav-item" type="button"><GearSix size={19} /><span>Configurações</span></button>
          <div className="sidebar-note"><span className="pulse-dot" /> Ambiente local</div>
        </div>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <button className="mobile-menu" type="button" aria-label="Abrir menu"><List size={23} /></button>
          <div className="branch-wrap">
            <button className="branch-select" type="button" onClick={() => setBranchOpen(!branchOpen)} aria-expanded={branchOpen}>
              <span className="branch-icon"><Storefront size={17} /></span>
              <span><small>FILIAL ATIVA</small><strong>ConstruLar — Centro</strong></span>
              <CaretDown size={16} />
            </button>
            {branchOpen && (
              <div className="floating-menu branch-menu">
                <button type="button"><Storefront size={17} /> ConstruLar — Centro <span className="selected-check">Atual</span></button>
                <button type="button"><Storefront size={17} /> ConstruLar — Depósito</button>
                <button type="button"><Buildings size={17} /> Gerenciar filiais</button>
              </div>
            )}
          </div>
          <div className="topbar-actions">
            <label className="search"><MagnifyingGlass size={18} /><input placeholder="Buscar no sistema" aria-label="Buscar no sistema" /></label>
            <div className="notification-wrap">
              <button className="icon-button notification-button" type="button" onClick={() => setNotificationsOpen(!notificationsOpen)} aria-label="Notificações" aria-expanded={notificationsOpen}>
                <Bell size={20} /><span className="notification-count">2</span>
              </button>
              {notificationsOpen && <Notifications onClose={() => setNotificationsOpen(false)} />}
            </div>
            <div className="profile-wrap">
              <button className="profile-button" type="button" onClick={() => setProfileOpen(!profileOpen)} aria-expanded={profileOpen}>
                <span className="avatar">AM</span><span className="profile-name">Ana Martins</span><CaretDown size={15} />
              </button>
              {profileOpen && (
                <div className="floating-menu profile-menu">
                  <div className="profile-summary"><span className="avatar avatar--large">AM</span><span><strong>Ana Martins</strong><small>Gerente de filial</small></span></div>
                  <button type="button"><UserCircle size={18} /> Meu perfil</button>
                  <button type="button" onClick={() => setScreen('login')}><SignOut size={18} /> Sair da demonstração</button>
                </div>
              )}
            </div>
          </div>
        </header>

        <section className="content">
          <div className="page-heading entry-reveal">
            <div><p className="eyebrow">OPERAÇÃO · 30 DE OUTUBRO</p><h1>Bom dia, Ana.</h1><p>Acompanhe o ritmo da filial e priorize o que move a operação.</p></div>
            <button className="outline-button" type="button"><ChartLineUp size={18} /> Ver relatórios</button>
          </div>

          <section className="metric-grid entry-reveal" aria-label="Indicadores da operação">
            <Metric label="Vendas hoje" value="R$ 18.460" detail="12,8% acima de ontem" tone="positive" />
            <Metric label="Pedidos em aberto" value="17" detail="5 com retirada para hoje" tone="neutral" />
            <Metric label="Itens com estoque baixo" value="8" detail="Revisar antes do próximo pedido" tone="warning" />
          </section>

          <section className="dashboard-grid entry-reveal">
            <div className="sales-panel panel">
              <div className="panel-header"><div><p className="eyebrow">FATURAMENTO</p><h2>Movimento da semana</h2></div><button className="text-button" type="button">Últimos 7 dias <CaretDown size={15} /></button></div>
              <div className="chart" aria-label="Gráfico demonstrativo de faturamento semanal">
                {[42, 61, 48, 74, 58, 89, 69].map((height, index) => <div className="bar-group" key={index}><span className="bar" style={{ height: `${height}%` }} /><small>{['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'][index]}</small></div>)}
              </div>
            </div>
            <div className="attention-panel panel">
              <div className="panel-header"><div><p className="eyebrow">ATENÇÃO</p><h2>Para acompanhar</h2></div><button className="icon-button" type="button" aria-label="Mais opções"><List size={19} /></button></div>
              <div className="attention-list">
                <Attention title="3 orçamentos aguardam retorno" meta="Próxima ação hoje" />
                <Attention title="8 itens abaixo do mínimo" meta="Estoque" />
                <Attention title="Caixa 02 abre às 08:00" meta="Rotina" />
              </div>
            </div>
          </section>
        </section>
      </main>
    </div>
  )
}

function Metric({ label, value, detail, tone }: { label: string; value: string; detail: string; tone: string }) {
  return <article className="metric"><p>{label}</p><strong>{value}</strong><span className={`metric-detail metric-detail--${tone}`}>{detail}</span></article>
}

function Attention({ title, meta }: { title: string; meta: string }) {
  return <button className="attention-row" type="button"><span className="attention-line" /><span><strong>{title}</strong><small>{meta}</small></span><CaretDown size={16} /></button>
}

function Notifications({ onClose }: { onClose: () => void }) {
  return <div className="notification-panel floating-menu">
    <div className="notification-heading"><div><p className="eyebrow">CENTRO DE ATUALIZAÇÕES</p><h2>Notificações</h2></div><button className="icon-button" type="button" onClick={onClose} aria-label="Fechar notificações"><X size={18} /></button></div>
    {notifications.map((notification) => <article className="notification-item" key={notification.title}><span className="notification-dot" /><div><strong>{notification.title}</strong><p>{notification.detail}</p><small>{notification.time}</small></div></article>)}
    <button className="text-button notification-footer" type="button">Ver todas as atualizações</button>
  </div>
}

function Login({ onEnter }: { onEnter: () => void }) {
  const [error, setError] = useState('')
  function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setError(''); onEnter() }
  return <main className="login-layout"><section className="login-intro"><div className="brand"><span className="brand-mark">M</span><span>MaterialCRM</span></div><div><p className="eyebrow">GESTÃO LOCAL PARA QUEM CONSTRÓI</p><h1>Uma operação clara começa com decisões visíveis.</h1><p>Vendas, estoque e relacionamento comercial em um sistema preparado para a rede da sua loja.</p></div><span className="login-status"><span className="pulse-dot" /> Servidor local disponível</span></section><section className="login-form-area"><form className="login-form" onSubmit={submit}><p className="eyebrow">ACESSO SEGURO</p><h2>Entrar no MaterialCRM</h2><p>Use suas credenciais para acessar a operação da filial.</p><label>E-mail<input required type="email" placeholder="nome@empresa.com" /></label><label>Senha<input required type="password" placeholder="Sua senha" /></label>{error && <p className="form-error">{error}</p>}<button className="primary-button" type="submit">Entrar no sistema</button><small>Autenticação demonstrativa para a Etapa 03.</small></form></section></main>
}

export default App

