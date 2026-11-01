import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Bell, Buildings, CaretDown, ChartLineUp, ClipboardText, Cube, GearSix, House, List, MagnifyingGlass, SignOut, Storefront, UsersThree, X } from '@phosphor-icons/react'

type Identity = { userId: string; email: string; role: string }
type LoginResponse = { accessToken: string; user: Identity }
type Branch = { id: string; name: string; code: string; active: boolean; selected: boolean }
type OrganizationContext = { company: { id: string; legalName: string; tradeName: string; taxId: string; active: boolean }; branches: Branch[]; role: string }
type Customer = { id: string; branchId: string; customerType: 'PF' | 'PJ'; name: string; document: string | null; email: string | null; phone: string | null; active: boolean }
type Product = { id: string; sku: string; name: string; category: string | null; unit: string; costPrice: number; salePrice: number; active: boolean }
type Stock = { id: string; productId: string; sku: string; name: string; quantity: number; minimumQuantity: number }
const TOKEN_KEY = 'materialcrm_access_token'
const navigation = [
  { label: 'Visão geral', icon: House },
  { label: 'Vendas', icon: ChartLineUp },
  { label: 'Clientes', icon: UsersThree },
  { label: 'Produtos', icon: Cube },
  { label: 'Estoque', icon: ClipboardText },
]
const notifications = [
  { title: 'Autenticação local', detail: 'A sessão é validada pela API da própria loja.', time: 'agora' },
  { title: 'Próxima entrega', detail: 'Vínculos empresariais e permissões por filial.', time: 'planejado' },
]
async function validateSession(token: string): Promise<Identity> {
  const response = await fetch('/api/v1/me', { headers: { Authorization: `Bearer ${token}` } })
  if (!response.ok) throw new Error('Sessão inválida ou expirada.')
  return response.json() as Promise<Identity>
}
async function loadOrganization(token: string): Promise<OrganizationContext> {
  const response = await fetch('/api/v1/organization/context', { headers: { Authorization: `Bearer ${token}` } })
  if (!response.ok) throw new Error('Não foi possível carregar a empresa e as filiais.')
  return response.json() as Promise<OrganizationContext>
}
function App() {
  const [token, setToken] = useState<string | null>(null)
  const [identity, setIdentity] = useState<Identity | null>(null)
  const [organization, setOrganization] = useState<OrganizationContext | null>(null)
  const [checking, setChecking] = useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [branchOpen, setBranchOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const [activeView, setActiveView] = useState('overview')
  useEffect(() => {
    const saved = sessionStorage.getItem(TOKEN_KEY)
    if (!saved) { setChecking(false); return }
    let active = true
    void validateSession(saved).then((user) => {
      if (active) { setToken(saved); setIdentity(user); void loadOrganization(saved).then(setOrganization).catch(() => undefined) }
    }).catch(() => sessionStorage.removeItem(TOKEN_KEY))
      .finally(() => { if (active) setChecking(false) })
    return () => { active = false }
  }, [])
  async function login(email: string, password: string) {
    setLoading(true); setError('')
    try {
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      if (response.status === 401) throw new Error('E-mail ou senha inválidos.')
      if (!response.ok) throw new Error('Não foi possível acessar a API local.')
      const result = await response.json() as LoginResponse
      sessionStorage.setItem(TOKEN_KEY, result.accessToken)
      setToken(result.accessToken); setIdentity(result.user)
      void loadOrganization(result.accessToken).then(setOrganization).catch(() => undefined)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha ao entrar no sistema.')
    } finally { setLoading(false) }
  }
  function logout() {
    sessionStorage.removeItem(TOKEN_KEY); setToken(null); setIdentity(null); setOrganization(null); setError('')
  }
  if (checking) return <main className="auth-loading"><span className="pulse-dot" /> Verificando sessão local...</main>
  if (!token || !identity) return <Login error={error} loading={loading} onSubmit={login} />
  const email = identity.email
  const initials = email.slice(0, 2).toUpperCase()
  const selectedBranch = organization?.branches.find((branch) => branch.selected) ?? organization?.branches[0]
  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><span className="brand-mark">M</span><span>MaterialCRM</span></div>
      <nav className="sidebar-nav" aria-label="Navegação principal">
        {navigation.map(({ label, icon: Icon }, index) => { const view = index === 0 ? 'overview' : index === 1 ? 'sales' : index === 2 ? 'customers' : index === 3 ? 'products' : index === 4 ? 'inventory' : label.toLowerCase(); return <button className={`nav-item ${activeView === view ? 'nav-item--active' : ''}`} key={label} type="button" disabled={!['overview', 'sales', 'customers', 'products', 'inventory'].includes(view)} onClick={() => setActiveView(view)}><Icon size={19} /><span>{label}</span></button> })}
      </nav>
      <div className="sidebar-bottom"><button className="nav-item" type="button" disabled><GearSix size={19} /><span>Configurações</span></button><div className="sidebar-note"><span className="pulse-dot" /> API local autenticada</div></div>
    </aside>
    <main className="workspace">
      <header className="topbar">
        <button className="mobile-menu" type="button" aria-label="Abrir menu"><List size={23} /></button>
        <div className="branch-wrap">
          <button className="branch-select" type="button" onClick={() => setBranchOpen(!branchOpen)} aria-expanded={branchOpen}><span className="branch-icon"><Storefront size={17} /></span><span><small>FILIAL ATIVA</small><strong>{selectedBranch?.name ?? 'Nenhuma filial atribuída'}</strong></span><CaretDown size={16} /></button>
          {branchOpen && <div className="floating-menu branch-menu">{organization?.branches.length ? organization.branches.map((branch) => <button key={branch.id} type="button" onClick={async () => { const response = await fetch(`/api/v1/organization/branches/${branch.id}/active`, { method: 'PUT', headers: { Authorization: `Bearer ${token}` } }); if (response.ok && token) { setOrganization(await loadOrganization(token)); setBranchOpen(false) } }}><Buildings size={17} /> {branch.name}</button>) : <button type="button" disabled><Buildings size={17} /> Nenhuma filial atribuída</button>}</div>}
        </div>
        <div className="topbar-actions">
          <label className="search"><MagnifyingGlass size={18} /><input placeholder="Buscar no sistema" aria-label="Buscar no sistema" disabled /></label>
          <div className="notification-wrap"><button className="icon-button notification-button" type="button" onClick={() => setNotificationsOpen(!notificationsOpen)} aria-label="Notificações"><Bell size={20} /><span className="notification-count">2</span></button>{notificationsOpen && <Notifications onClose={() => setNotificationsOpen(false)} />}</div>
          <div className="profile-wrap">
            <button className="profile-button" type="button" onClick={() => setProfileOpen(!profileOpen)}><span className="avatar">{initials}</span><span className="profile-name">{email}</span><CaretDown size={15} /></button>
            {profileOpen && <div className="floating-menu profile-menu"><div className="profile-summary"><span className="avatar avatar--large">{initials}</span><span><strong>{email}</strong><small>Conta local · {identity.role}</small></span></div><button type="button" onClick={logout}><SignOut size={18} /> Sair</button></div>}
          </div>
        </div>
      </header>
      {activeView === 'customers' ? <Customers token={token} organization={organization} /> : activeView === 'products' ? <Products token={token} /> : activeView === 'inventory' ? <Inventory token={token} /> : activeView === 'sales' ? <Sales token={token} /> : <section className="content">
        <div className="page-heading entry-reveal"><div><p className="eyebrow">SESSÃO LOCAL VALIDADA</p><h1>Acesso confirmado.</h1><p>Sua identidade foi autenticada pelo MaterialCRM Local.</p></div><span className="outline-button"><ChartLineUp size={18} /> Fundação técnica</span></div>
        <section className="metric-grid entry-reveal" aria-label="Estado da integração">
          <Metric label="Identidade autenticada" value="Confirmada" detail={email} tone="positive" />
          <Metric label="API local" value="Conectada" detail={`Usuário ${identity.userId.slice(0, 8)}…`} tone="positive" />
          <Metric label="Perfil de acesso" value={identity.role} detail={organization?.company.tradeName ?? 'Empresa local'} tone="warning" />
        </section>
        <section className="dashboard-grid entry-reveal">
          <div className="sales-panel panel"><div className="panel-header"><div><p className="eyebrow">PRÓXIMA ENTREGA</p><h2>Preparação da operação</h2></div></div><div className="setup-steps">
            <div className="setup-step setup-step--done"><span>01</span><div><strong>PostgreSQL local</strong><small>Banco operacional no servidor da loja</small></div></div>
            <div className="setup-step setup-step--done"><span>02</span><div><strong>Autenticação local</strong><small>Senha protegida e token JWT local</small></div></div>
            <div className="setup-step"><span>03</span><div><strong>Vínculo de empresa e filial</strong><small>Próximo controle de autorização</small></div></div>
            <div className="setup-step"><span>04</span><div><strong>Dashboard com dados reais</strong><small>Depois da autorização empresarial</small></div></div>
          </div></div>
          <div className="attention-panel panel"><div className="panel-header"><div><p className="eyebrow">ACESSO</p><h2>Ambiente protegido</h2></div></div><div className="access-note"><span className="pulse-dot" /><p>As áreas comerciais permanecem bloqueadas até a conclusão das permissões por empresa e filial.</p></div><button className="text-button signout-inline" type="button" onClick={logout}>Encerrar sessão <SignOut size={16} /></button></div>
        </section>
      </section>}
    </main>
  </div>
}
function Customers({ token, organization }: { token: string; organization: OrganizationContext | null }) {
  const [customers, setCustomers] = useState<Customer[]>([])
  const [search, setSearch] = useState('')
  const [openForm, setOpenForm] = useState(false)
  const [editing, setEditing] = useState<Customer | null>(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState({ customerType: 'PJ', name: '', document: '', email: '', phone: '', branchId: '' })

  async function loadCustomers(term = search) {
    const response = await fetch(`/api/v1/customers${term ? `?search=${encodeURIComponent(term)}` : ''}`, { headers: { Authorization: `Bearer ${token}` } })
    if (!response.ok) throw new Error('Não foi possível carregar os clientes.')
    setCustomers(await response.json() as Customer[])
  }
  useEffect(() => { void loadCustomers().catch((cause) => setError(cause instanceof Error ? cause.message : 'Falha ao carregar clientes.')) }, [])
  function startCreate() { setEditing(null); setForm({ customerType: 'PJ', name: '', document: '', email: '', phone: '', branchId: organization?.branches.find((branch) => branch.selected)?.id ?? '' }); setOpenForm(true); setError(''); setMessage('') }
  function startEdit(customer: Customer) { setEditing(customer); setForm({ customerType: customer.customerType, name: customer.name, document: customer.document ?? '', email: customer.email ?? '', phone: customer.phone ?? '', branchId: customer.branchId }); setOpenForm(true); setError(''); setMessage('') }
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); setMessage('')
    const response = await fetch(editing ? `/api/v1/customers/${editing.id}` : '/api/v1/customers', { method: editing ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, body: JSON.stringify({ ...form, branchId: form.branchId || null }) })
    if (!response.ok) { const detail = await response.text(); setError(detail || 'Não foi possível salvar o cliente.'); return }
    setOpenForm(false); setMessage(editing ? 'Cliente atualizado.' : 'Cliente cadastrado.'); await loadCustomers()
  }
  async function deactivate(id: string) { if (!window.confirm('Desativar este cliente?')) return; const response = await fetch(`/api/v1/customers/${id}`, { method: 'DELETE', headers: { Authorization: `Bearer ${token}` } }); if (response.ok) { setMessage('Cliente desativado.'); await loadCustomers() } else setError('Não foi possível desativar o cliente.') }
  return <section className="content">
    <div className="page-heading entry-reveal"><div><p className="eyebrow">CADASTRO COMERCIAL</p><h1>Clientes.</h1><p>Cadastre e consulte os clientes da empresa e da filial ativa.</p></div><button className="primary-button customer-new-button" type="button" onClick={startCreate}>Novo cliente</button></div>
    {message && <p className="success-message">{message}</p>}{error && <p className="form-error">{error}</p>}
    <section className="panel customer-panel"><div className="customer-toolbar"><label className="search customer-search"><MagnifyingGlass size={18} /><input value={search} onChange={(event) => setSearch(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter') void loadCustomers() }} placeholder="Buscar por nome ou documento" /></label><span className="customer-count">{customers.length} cliente(s)</span></div>
      <div className="customer-table-wrap"><table className="customer-table"><thead><tr><th>Cliente</th><th>Tipo</th><th>Documento</th><th>Contato</th><th>Status</th><th></th></tr></thead><tbody>{customers.map((customer) => <tr key={customer.id}><td><strong>{customer.name}</strong><small>{customer.email ?? 'Sem e-mail'}</small></td><td>{customer.customerType}</td><td>{customer.document ?? '—'}</td><td>{customer.phone ?? '—'}</td><td><span className={customer.active ? 'status-active' : 'status-inactive'}>{customer.active ? 'Ativo' : 'Inativo'}</span></td><td className="customer-actions"><button type="button" onClick={() => startEdit(customer)}>Editar</button>{customer.active && <button type="button" onClick={() => void deactivate(customer.id)}>Desativar</button>}</td></tr>)}{customers.length === 0 && <tr><td colSpan={6} className="empty-state">Nenhum cliente encontrado.</td></tr>}</tbody></table></div>
    </section>
    {openForm && <div className="modal-backdrop"><form className="customer-form panel" onSubmit={submit}><div className="panel-header"><div><p className="eyebrow">{editing ? 'EDIÇÃO' : 'NOVO CADASTRO'}</p><h2>{editing ? 'Editar cliente' : 'Cadastrar cliente'}</h2></div><button className="icon-button" type="button" onClick={() => setOpenForm(false)} aria-label="Fechar"><X size={19} /></button></div><div className="customer-form-fields"><label>Tipo<select value={form.customerType} onChange={(event) => setForm({ ...form, customerType: event.target.value })}><option value="PJ">Pessoa jurídica</option><option value="PF">Pessoa física</option></select></label><label>Nome<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label><label>Documento<input value={form.document} onChange={(event) => setForm({ ...form, document: event.target.value })} /></label><label>E-mail<input type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label><label>Telefone<input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></label><label>Filial<select value={form.branchId} onChange={(event) => setForm({ ...form, branchId: event.target.value })}>{organization?.branches.map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></label></div><button className="primary-button" type="submit">Salvar cliente</button></form></div>}
  </section>
}
function Products({ token }: { token: string }) {
  const [products, setProducts] = useState<Product[]>([])
  const [search, setSearch] = useState('')
  const [openForm, setOpenForm] = useState(false)
  const [editing, setEditing] = useState<Product | null>(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState({ sku: '', name: '', category: '', unit: 'UN', costPrice: '', salePrice: '' })
  async function loadProducts(term = search) { const response = await fetch(`/api/v1/products${term ? `?search=${encodeURIComponent(term)}` : ''}`, { headers: { Authorization: `Bearer ${token}` } }); if (!response.ok) throw new Error('Não foi possível carregar os produtos.'); setProducts(await response.json() as Product[]) }
  useEffect(() => { void loadProducts().catch((cause) => setError(cause instanceof Error ? cause.message : 'Falha ao carregar produtos.')) }, [])
  function startCreate() { setEditing(null); setForm({ sku: '', name: '', category: '', unit: 'UN', costPrice: '', salePrice: '' }); setOpenForm(true); setError(''); setMessage('') }
  function startEdit(product: Product) { setEditing(product); setForm({ sku: product.sku, name: product.name, category: product.category ?? '', unit: product.unit, costPrice: String(product.costPrice), salePrice: String(product.salePrice) }); setOpenForm(true); setError(''); setMessage('') }
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setError(''); setMessage(''); const body = { ...form, costPrice: Number(form.costPrice), salePrice: Number(form.salePrice) }; const response = await fetch(editing ? `/api/v1/products/${editing.id}` : '/api/v1/products', { method: editing ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, body: JSON.stringify(body) }); if (!response.ok) { setError((await response.text()) || 'Não foi possível salvar o produto.'); return } setOpenForm(false); setMessage(editing ? 'Produto atualizado.' : 'Produto cadastrado.'); await loadProducts() }
  async function deactivate(id: string) { if (!window.confirm('Desativar este produto?')) return; const response = await fetch(`/api/v1/products/${id}`, { method: 'DELETE', headers: { Authorization: `Bearer ${token}` } }); if (response.ok) { setMessage('Produto desativado.'); await loadProducts() } else setError('Não foi possível desativar o produto.') }
  return <section className="content"><div className="page-heading entry-reveal"><div><p className="eyebrow">CATÁLOGO COMERCIAL</p><h1>Produtos.</h1><p>Cadastre os materiais comercializados pela empresa.</p></div><button className="primary-button customer-new-button" type="button" onClick={startCreate}>Novo produto</button></div>{message && <p className="success-message">{message}</p>}{error && <p className="form-error">{error}</p>}<section className="panel customer-panel"><div className="customer-toolbar"><label className="search customer-search"><MagnifyingGlass size={18} /><input value={search} onChange={(event) => setSearch(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter') void loadProducts() }} placeholder="Buscar por nome ou SKU" /></label><span className="customer-count">{products.length} produto(s)</span></div><div className="customer-table-wrap"><table className="customer-table"><thead><tr><th>Produto</th><th>SKU</th><th>Categoria</th><th>Preço de venda</th><th>Status</th><th></th></tr></thead><tbody>{products.map((product) => <tr key={product.id}><td><strong>{product.name}</strong><small>{product.unit}</small></td><td>{product.sku}</td><td>{product.category ?? '—'}</td><td>R$ {product.salePrice.toFixed(2).replace('.', ',')}</td><td><span className={product.active ? 'status-active' : 'status-inactive'}>{product.active ? 'Ativo' : 'Inativo'}</span></td><td className="customer-actions"><button type="button" onClick={() => startEdit(product)}>Editar</button>{product.active && <button type="button" onClick={() => void deactivate(product.id)}>Desativar</button>}</td></tr>)}{products.length === 0 && <tr><td colSpan={6} className="empty-state">Nenhum produto encontrado.</td></tr>}</tbody></table></div></section>{openForm && <div className="modal-backdrop"><form className="customer-form panel" onSubmit={submit}><div className="panel-header"><div><p className="eyebrow">{editing ? 'EDIÇÃO' : 'NOVO CADASTRO'}</p><h2>{editing ? 'Editar produto' : 'Cadastrar produto'}</h2></div><button className="icon-button" type="button" onClick={() => setOpenForm(false)} aria-label="Fechar"><X size={19} /></button></div><div className="customer-form-fields"><label>SKU<input required value={form.sku} onChange={(event) => setForm({ ...form, sku: event.target.value })} /></label><label>Nome<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label><label>Categoria<input value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} /></label><label>Unidade<input required value={form.unit} onChange={(event) => setForm({ ...form, unit: event.target.value })} /></label><label>Preço de custo<input required type="number" min="0" step="0.01" value={form.costPrice} onChange={(event) => setForm({ ...form, costPrice: event.target.value })} /></label><label>Preço de venda<input required type="number" min="0" step="0.01" value={form.salePrice} onChange={(event) => setForm({ ...form, salePrice: event.target.value })} /></label></div><button className="primary-button" type="submit">Salvar produto</button></form></div>}</section>
}
function Inventory({ token }: { token: string }) {
  const [stock, setStock] = useState<Stock[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [openForm, setOpenForm] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState({ productId: '', movementType: 'IN', quantity: '', note: '' })
  async function loadStock() { const headers = { Authorization: `Bearer ${token}` }; const [stockResponse, productsResponse] = await Promise.all([fetch('/api/v1/inventory', { headers }), fetch('/api/v1/products', { headers })]); if (!stockResponse.ok || !productsResponse.ok) throw new Error('Não foi possível carregar o estoque.'); setStock(await stockResponse.json() as Stock[]); setProducts(await productsResponse.json() as Product[]) }
  useEffect(() => { void loadStock().catch((cause) => setError(cause instanceof Error ? cause.message : 'Falha ao carregar estoque.')) }, [])
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setError(''); setMessage(''); const response = await fetch('/api/v1/inventory/movements', { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, body: JSON.stringify({ ...form, quantity: Number(form.quantity) }) }); if (!response.ok) { setError((await response.text()) || 'Não foi possível registrar a movimentação.'); return } setOpenForm(false); setMessage('Movimentação registrada.'); setForm({ productId: '', movementType: 'IN', quantity: '', note: '' }); await loadStock() }
  return <section className="content"><div className="page-heading entry-reveal"><div><p className="eyebrow">OPERAÇÃO DE ESTOQUE</p><h1>Estoque.</h1><p>Acompanhe as quantidades da filial ativa e registre movimentações.</p></div><button className="primary-button customer-new-button" type="button" onClick={() => { setOpenForm(true); setError(''); setMessage('') }}>Nova movimentação</button></div>{message && <p className="success-message">{message}</p>}{error && <p className="form-error">{error}</p>}<section className="panel customer-panel"><div className="customer-toolbar"><span className="customer-count">{stock.length} produto(s) no estoque</span><span className="customer-count">Valores abaixo do mínimo exigem reposição</span></div><div className="customer-table-wrap"><table className="customer-table"><thead><tr><th>Produto</th><th>SKU</th><th>Quantidade</th><th>Mínimo</th><th>Status</th></tr></thead><tbody>{stock.map((item) => <tr key={item.id}><td><strong>{item.name}</strong></td><td>{item.sku}</td><td>{item.quantity}</td><td>{item.minimumQuantity}</td><td><span className={item.quantity <= item.minimumQuantity ? 'status-inactive' : 'status-active'}>{item.quantity <= item.minimumQuantity ? 'Reposição' : 'Normal'}</span></td></tr>)}{stock.length === 0 && <tr><td colSpan={5} className="empty-state">Nenhum produto possui movimentação nesta filial.</td></tr>}</tbody></table></div></section>{openForm && <div className="modal-backdrop"><form className="customer-form panel" onSubmit={submit}><div className="panel-header"><div><p className="eyebrow">MOVIMENTAÇÃO</p><h2>Registrar estoque</h2></div><button className="icon-button" type="button" onClick={() => setOpenForm(false)} aria-label="Fechar"><X size={19} /></button></div><div className="customer-form-fields"><label>Produto<select required value={form.productId} onChange={(event) => setForm({ ...form, productId: event.target.value })}><option value="">Selecione um produto</option>{products.filter((product) => product.active).map((product) => <option key={product.id} value={product.id}>{product.sku} — {product.name}</option>)}</select></label><label>Tipo<select value={form.movementType} onChange={(event) => setForm({ ...form, movementType: event.target.value })}><option value="IN">Entrada</option><option value="OUT">Saída</option><option value="ADJUST">Ajuste</option></select></label><label>Quantidade<input required type="number" min="0.001" step="0.001" value={form.quantity} onChange={(event) => setForm({ ...form, quantity: event.target.value })} /></label><label>Observação<input value={form.note} onChange={(event) => setForm({ ...form, note: event.target.value })} /></label></div><button className="primary-button" type="submit">Registrar movimentação</button></form></div>}</section>
}
function Sales({ token }: { token: string }) {
  const [sales, setSales] = useState<Array<{ id: string; paymentMethod: string; status: string; total: number; createdAt: string }>>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [customerId, setCustomerId] = useState('')
  const [productId, setProductId] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [paymentMethod, setPaymentMethod] = useState('PIX')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  async function load() { const headers = { Authorization: `Bearer ${token}` }; const [salesResponse, customersResponse, productsResponse] = await Promise.all([fetch('/api/v1/sales', { headers }), fetch('/api/v1/customers', { headers }), fetch('/api/v1/products', { headers })]); if (!salesResponse.ok || !customersResponse.ok || !productsResponse.ok) throw new Error('Não foi possível carregar os dados da venda.'); setSales(await salesResponse.json()); setCustomers(await customersResponse.json()); setProducts(await productsResponse.json()) }
  useEffect(() => { void load().catch((cause) => setError(cause instanceof Error ? cause.message : 'Falha ao carregar vendas.')) }, [])
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setError(''); setMessage(''); if (!productId || Number(quantity) <= 0) { setError('Selecione um produto e informe uma quantidade válida.'); return } const response = await fetch('/api/v1/sales', { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, body: JSON.stringify({ customerId: customerId || null, paymentMethod, items: [{ productId, quantity: Number(quantity) }] }) }); if (!response.ok) { setError((await response.text()) || 'Não foi possível finalizar a venda.'); return } setMessage('Venda registrada e estoque atualizado.'); setCustomerId(''); setProductId(''); setQuantity('1'); await load() }
  return <section className="content"><div className="page-heading entry-reveal"><div><p className="eyebrow">OPERAÇÃO COMERCIAL</p><h1>Vendas.</h1><p>Registre vendas e desconte automaticamente os produtos do estoque.</p></div></div>{message && <p className="success-message">{message}</p>}{error && <p className="form-error">{error}</p>}<section className="dashboard-grid"><form className="panel customer-form sales-form" onSubmit={submit}><div className="panel-header"><div><p className="eyebrow">NOVA VENDA</p><h2>Registrar venda</h2></div></div><div className="customer-form-fields"><label>Cliente<select value={customerId} onChange={(event) => setCustomerId(event.target.value)}><option value="">Consumidor não identificado</option>{customers.filter((customer) => customer.active).map((customer) => <option key={customer.id} value={customer.id}>{customer.name}</option>)}</select></label><label>Forma de pagamento<select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}><option value="PIX">PIX</option><option value="CREDIT_CARD">Cartão de crédito</option><option value="DEBIT_CARD">Cartão de débito</option><option value="CASH">Dinheiro</option><option value="TRANSFER">Transferência</option></select></label><label>Produto<select required value={productId} onChange={(event) => setProductId(event.target.value)}><option value="">Selecione um produto</option>{products.filter((product) => product.active).map((product) => <option key={product.id} value={product.id}>{product.sku} — {product.name}</option>)}</select></label><label>Quantidade<input required type="number" min="0.001" step="0.001" value={quantity} onChange={(event) => setQuantity(event.target.value)} /></label></div><button className="primary-button" type="submit">Finalizar venda</button></form><section className="panel"><div className="panel-header"><div><p className="eyebrow">HISTÓRICO RECENTE</p><h2>Vendas da filial</h2></div></div><div className="attention-list">{sales.map((sale) => <div className="attention-row" key={sale.id}><span className="attention-line" /><span><strong>{sale.paymentMethod}</strong><small>{new Date(sale.createdAt).toLocaleString('pt-BR')}</small></span><strong>R$ {sale.total.toFixed(2).replace('.', ',')}</strong></div>)}{sales.length === 0 && <p className="empty-state">Nenhuma venda registrada.</p>}</div></section></section></section>
}
function Metric({ label, value, detail, tone }: { label: string; value: string; detail: string; tone: string }) {
  return <article className="metric"><p>{label}</p><strong>{value}</strong><span className={`metric-detail metric-detail--${tone}`}>{detail}</span></article>
}
function Notifications({ onClose }: { onClose: () => void }) {
  return <div className="notification-panel floating-menu"><div className="notification-heading"><div><p className="eyebrow">CENTRO DE ATUALIZAÇÕES</p><h2>Notificações</h2></div><button className="icon-button" type="button" onClick={onClose} aria-label="Fechar notificações"><X size={18} /></button></div>{notifications.map((item) => <article className="notification-item" key={item.title}><span className="notification-dot" /><div><strong>{item.title}</strong><p>{item.detail}</p><small>{item.time}</small></div></article>)}</div>
}
function Login({ error, loading, onSubmit }: { error: string; loading: boolean; onSubmit: (email: string, password: string) => Promise<void> }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); void onSubmit(email.trim(), password) }
  return <main className="login-layout">
    <section className="login-intro"><div className="brand"><span className="brand-mark">M</span><span>MaterialCRM</span></div><div><p className="eyebrow">GESTÃO LOCAL PARA QUEM CONSTRÓI</p><h1>Uma operação clara começa com decisões visíveis.</h1><p>Vendas, estoque e relacionamento comercial em um sistema preparado para a rede da sua loja.</p></div><span className="login-status"><span className="pulse-dot" /> Autenticação no servidor local</span></section>
    <section className="login-form-area"><form className="login-form" onSubmit={submit}><p className="eyebrow">ACESSO SEGURO</p><h2>Entrar no MaterialCRM</h2><p>Use a conta de funcionário cadastrada neste servidor.</p>
      <label>E-mail<input required type="email" autoComplete="username" placeholder="nome@empresa.com" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
      <label>Senha<input required type="password" autoComplete="current-password" placeholder="Sua senha" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
      {error && <p className="form-error" role="alert">{error}</p>}
      <button className="primary-button" type="submit" disabled={loading}>{loading ? 'Validando acesso...' : 'Entrar no sistema'}</button><small>Cadastro público desativado. Contas são provisionadas por administrador.</small>
    </form></section>
  </main>
}
export default App
