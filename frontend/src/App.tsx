import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import type { Session } from '@supabase/supabase-js'
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
import { supabase, supabaseConfigReady } from './supabase'

type ApiIdentity = { userId: string }

const navigation = [
  { label: 'Visão geral', icon: House },
  { label: 'Vendas', icon: ChartLineUp },
  { label: 'Clientes', icon: UsersThree },
  { label: 'Produtos', icon: Cube },
  { label: 'Estoque', icon: ClipboardText },
]

const notifications = [
  { title: 'Fundação técnica', detail: 'A autenticação está conectada ao Supabase.', time: 'agora' },
  { title: 'Próxima etapa', detail: 'Os módulos comerciais serão liberados após a autorização por filial.', time: 'planejado' },
]

async function validateApiSession(accessToken: string): Promise<ApiIdentity> {
  const response = await fetch('/api/v1/me', {
    headers: { Authorization: `Bearer ${accessToken}` },
  })

  if (response.status === 401) {
    throw new Error('O Supabase autenticou sua conta, mas a API rejeitou o token. Confira a configuração de Signing Keys do projeto e se ela usa ES256 ou RS256.')
  }

  if (!response.ok) {
    throw new Error(`A API local respondeu com o status ${response.status}. Verifique o backend.`)
  }

  return response.json() as Promise<ApiIdentity>
}

function App() {
  const [session, setSession] = useState<Session | null>(null)
  const [identity, setIdentity] = useState<ApiIdentity | null>(null)
  const [checking, setChecking] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [loginError, setLoginError] = useState('')
  const [branchOpen, setBranchOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)

  useEffect(() => {
    if (!supabase) {
      setChecking(false)
      return
    }

    let active = true

    const loadSession = async () => {
      const { data, error } = await supabase.auth.getSession()
      if (!active) return

      if (error) {
        setLoginError('Não foi possível recuperar sua sessão. Entre novamente.')
        setChecking(false)
        return
      }

      setSession(data.session)
      if (data.session) {
        try {
          const currentIdentity = await validateApiSession(data.session.access_token)
          if (active) setIdentity(currentIdentity)
        } catch (error) {
          if (active) setLoginError(error instanceof Error ? error.message : 'Não foi possível validar o acesso à API.')
        }
      }
      if (active) setChecking(false)
    }

    void loadSession()

    const { data: authListener } = supabase.auth.onAuthStateChange((_event, nextSession) => {
      setSession(nextSession)
      if (!nextSession) setIdentity(null)
    })

    return () => {
      active = false
      authListener.subscription.unsubscribe()
    }
  }, [])

  async function handleLogin(email: string, password: string) {
    if (!supabase) {
      setLoginError('Configuração do Supabase ausente. Preencha as variáveis VITE_SUPABASE_URL e VITE_SUPABASE_PUBLISHABLE_KEY no arquivo .env e reconstrua o frontend.')
      return
    }

    setSubmitting(true)
    setLoginError('')
    try {
      const { data, error } = await supabase.auth.signInWithPassword({ email, password })
      if (error) throw error
      if (!data.session) throw new Error('O Supabase não retornou uma sessão válida.')

      setSession(data.session)
      const currentIdentity = await validateApiSession(data.session.access_token)
      setIdentity(currentIdentity)
    } catch (error) {
      setIdentity(null)
      setLoginError(error instanceof Error ? error.message : 'Não foi possível entrar. Tente novamente.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleSignOut() {
    if (!supabase) return
    await supabase.auth.signOut()
    setSession(null)
    setIdentity(null)
    setLoginError('')
  }

  if (checking) {
    return <main className="auth-loading"><span className="pulse-dot" /> Verificando sessão segura...</main>
  }

  if (!session || !identity) {
    return <Login
      error={loginError}
      loading={submitting}
      configured={supabaseConfigReady}
      onSubmit={handleLogin}
    />
  }

  const email = session.user.email ?? 'Usuário autenticado'
  const initials = email.slice(0, 2).toUpperCase()

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark">M</span><span>MaterialCRM</span></div>
        <nav className="sidebar-nav" aria-label="Navegação principal">
          {navigation.map(({ label, icon: Icon }, index) => (
            <button className={`nav-item ${index === 0 ? 'nav-item--active' : ''}`} key={label} type="button" disabled={index !== 0}>
              <Icon size={19} weight="regular" />
              <span>{label}</span>
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <button className="nav-item" type="button" disabled><GearSix size={19} /><span>Configurações</span></button>
          <div className="sidebar-note"><span className="pulse-dot" /> API local autenticada</div>
        </div>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <button className="mobile-menu" type="button" aria-label="Abrir menu"><List size={23} /></button>
          <div className="branch-wrap">
            <button className="branch-select" type="button" onClick={() => setBranchOpen(!branchOpen)} aria-expanded={branchOpen}>
              <span className="branch-icon"><Storefront size={17} /></span>
              <span><small>FILIAL ATIVA</small><strong>Filial ainda não vinculada</strong></span>
              <CaretDown size={16} />
            </button>
            {branchOpen && (
              <div className="floating-menu branch-menu">
                <button type="button" disabled><Buildings size={17} /> Nenhuma filial atribuída</button>
              </div>
            )}
          </div>
          <div className="topbar-actions">
            <label className="search"><MagnifyingGlass size={18} /><input placeholder="Buscar no sistema" aria-label="Buscar no sistema" disabled /></label>
            <div className="notification-wrap">
              <button className="icon-button notification-button" type="button" onClick={() => setNotificationsOpen(!notificationsOpen)} aria-label="Notificações" aria-expanded={notificationsOpen}>
                <Bell size={20} /><span className="notification-count">2</span>
              </button>
              {notificationsOpen && <Notifications onClose={() => setNotificationsOpen(false)} />}
            </div>
            <div className="profile-wrap">
              <button className="profile-button" type="button" onClick={() => setProfileOpen(!profileOpen)} aria-expanded={profileOpen}>
                <span className="avatar">{initials}</span><span className="profile-name">{email}</span><CaretDown size={15} />
              </button>
              {profileOpen && (
                <div className="floating-menu profile-menu">
                  <div className="profile-summary"><span className="avatar avatar--large">{initials}</span><span><strong>{email}</strong><small>Conta Supabase autenticada</small></span></div>
                  <button type="button"><UserCircle size={18} /> Minha conta</button>
                  <button type="button" onClick={() => void handleSignOut()}><SignOut size={18} /> Sair</button>
                </div>
              )}
            </div>
          </div>
        </header>

        <section className="content">
          <div className="page-heading entry-reveal">
            <div><p className="eyebrow">SESSÃO VALIDADA · AMBIENTE LOCAL</p><h1>Acesso confirmado.</h1><p>Sua identidade foi confirmada pelo Supabase Auth e validada pela API local.</p></div>
            <span className="outline-button"><ChartLineUp size={18} /> Fundação técnica</span>
          </div>

          <section className="metric-grid entry-reveal" aria-label="Estado da integração">
            <Metric label="Identidade autenticada" value="Confirmada" detail={email} tone="positive" />
            <Metric label="API local" value="Conectada" detail={`Usuário ${identity.userId.slice(0, 8)}…`} tone="positive" />
            <Metric label="Acesso empresarial" value="Pendente" detail="Aguardando vínculo com empresa e filial" tone="warning" />
          </section>

          <section className="dashboard-grid entry-reveal">
            <div className="sales-panel panel">
              <div className="panel-header"><div><p className="eyebrow">PRÓXIMA ENTREGA</p><h2>Preparação da operação</h2></div></div>
              <div className="setup-steps">
                <div className="setup-step setup-step--done"><span>01</span><div><strong>Login Supabase</strong><small>Identidade e sessão verificadas</small></div></div>
                <div className="setup-step setup-step--done"><span>02</span><div><strong>Validação JWT na API</strong><small>Token aceito pelo Spring Security</small></div></div>
                <div className="setup-step"><span>03</span><div><strong>Vínculo de empresa e filial</strong><small>Próximo controle de autorização</small></div></div>
                <div className="setup-step"><span>04</span><div><strong>Dashboard com dados reais</strong><small>Depois da autorização empresarial</small></div></div>
              </div>
            </div>
            <div className="attention-panel panel">
              <div className="panel-header"><div><p className="eyebrow">ACESSO</p><h2>Ambiente protegido</h2></div></div>
              <div className="access-note"><span className="pulse-dot" /><p>Você entrou com uma conta real. As áreas de vendas, clientes e estoque permanecem bloqueadas até a conclusão das permissões por empresa e filial.</p></div>
              <button className="text-button signout-inline" type="button" onClick={() => void handleSignOut()}>Encerrar sessão <SignOut size={16} /></button>
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

function Notifications({ onClose }: { onClose: () => void }) {
  return <div className="notification-panel floating-menu">
    <div className="notification-heading"><div><p className="eyebrow">CENTRO DE ATUALIZAÇÕES</p><h2>Notificações</h2></div><button className="icon-button" type="button" onClick={onClose} aria-label="Fechar notificações"><X size={18} /></button></div>
    {notifications.map((notification) => <article className="notification-item" key={notification.title}><span className="notification-dot" /><div><strong>{notification.title}</strong><p>{notification.detail}</p><small>{notification.time}</small></div></article>)}
  </div>
}

function Login({ error, loading, configured, onSubmit }: {
  error: string
  loading: boolean
  configured: boolean
  onSubmit: (email: string, password: string) => Promise<void>
}) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void onSubmit(email.trim(), password)
  }

  return <main className="login-layout">
    <section className="login-intro">
      <div className="brand"><span className="brand-mark">M</span><span>MaterialCRM</span></div>
      <div><p className="eyebrow">GESTÃO LOCAL PARA QUEM CONSTRÓI</p><h1>Uma operação clara começa com decisões visíveis.</h1><p>Vendas, estoque e relacionamento comercial em um sistema preparado para a rede da sua loja.</p></div>
      <span className="login-status"><span className="pulse-dot" /> Aplicação executada localmente</span>
    </section>
    <section className="login-form-area">
      <form className="login-form" onSubmit={submit}>
        <p className="eyebrow">ACESSO SEGURO</p>
        <h2>Entrar no MaterialCRM</h2>
        <p>Entre com a conta de funcionário cadastrada no Supabase Auth.</p>
        <label>E-mail<input required type="email" autoComplete="username" placeholder="nome@empresa.com" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>Senha<input required type="password" autoComplete="current-password" placeholder="Sua senha" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        {error && <p className="form-error" role="alert">{error}</p>}
        {!configured && <p className="form-error" role="alert">Configuração do Supabase ausente. Preencha as variáveis VITE_SUPABASE_URL e VITE_SUPABASE_PUBLISHABLE_KEY no .env.</p>}
        <button className="primary-button" type="submit" disabled={loading || !configured}>{loading ? 'Validando acesso...' : 'Entrar no sistema'}</button>
        <small>Cadastro público desativado. Contas devem ser criadas por um administrador.</small>
      </form>
    </section>
  </main>
}

export default App
