// Configurações Globais
const API_BASE_URL = 'https://apipizaria2.onrender.com';

// Estado da Aplicação
const state = {
    apiKey: localStorage.getItem('bellaroza_api_key') || 'api-pizzaria-secret-key-272',
    activeTab: 'tab-dashboard',
    selectedClientId: null,
    selectedClientName: '',
    cart: [],
    productsList: [], // Cache de produtos para o carrinho
    pagination: {
        clientes: { page: 0, size: 5, totalPages: 0, currentSearch: '' },
        enderecos: { page: 0, size: 10, totalPages: 0 },
        produtos: { page: 0, size: 6, totalPages: 0, currentSize: '' },
        ingredientes: { page: 0, size: 5, totalPages: 0, currentSearch: '' },
        pedidos: { page: 0, size: 10, totalPages: 0, currentStatus: '' }
    }
};

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    // Carregar chave de API salva
    document.getElementById('input-api-key').value = state.apiKey;
    
    // Iniciar eventos
    initEventListeners();
    
    // Testar conexão inicial
    pingAPI();
    
    // Gerar chaves de idempotência iniciais
    refreshIdempotencyKeys();
});

// Geração de UUID para Idempotency Key
function generateUUID() {
    return 'idemp-' + 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

function refreshIdempotencyKeys() {
    const clientKey = generateUUID();
    const orderKey = generateUUID();
    
    const clientInput = document.getElementById('client-idempotency-key');
    if (clientInput) clientInput.value = clientKey;
    
    const orderInput = document.getElementById('cart-idempotency-key');
    if (orderInput) orderInput.value = orderKey;
}

// Custom Toast Notifications
function showToast(title, message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'fa-check-circle';
    if (type === 'error') icon = 'fa-exclamation-circle';
    if (type === 'warning') icon = 'fa-triangle-exclamation';
    
    toast.innerHTML = `
        <div class="toast-icon"><i class="fa-solid ${icon}"></i></div>
        <div class="toast-body">
            <div class="toast-title">${title}</div>
            <div class="toast-desc">${message}</div>
        </div>
        <span class="toast-close">&times;</span>
    `;
    
    container.appendChild(toast);
    
    // Evento de fechar manual
    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100px)';
        setTimeout(() => toast.remove(), 300);
    });
    
    // Auto-remove após 4 segundos
    setTimeout(() => {
        if (toast.parentNode) {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100px)';
            setTimeout(() => toast.remove(), 300);
        }
    }, 4500);
}

// API Inspector logger
function logAPIRequest(method, url, headers, body) {
    const reqTextEl = document.getElementById('inspector-req-text');
    let headersStr = '';
    for (const [key, value] of Object.entries(headers)) {
        headersStr += `${key}: ${value}\n`;
    }
    
    let bodyStr = '';
    if (body) {
        bodyStr = typeof body === 'string' ? body : JSON.stringify(body, null, 2);
    }
    
    reqTextEl.innerHTML = `<span class="http-method-${method.toLowerCase()}">${method}</span> ${url}\n\n[Headers]\n${headersStr}\n[Body]\n${bodyStr || '(Vazio)'}`;
}

function logAPIResponse(status, statusText, headers, bodyText) {
    const resTextEl = document.getElementById('inspector-res-text');
    let statusClass = 'http-status-2xx';
    if (status >= 400) statusClass = 'http-status-4xx';
    if (status === 429) statusClass = 'http-status-429';
    
    let headersStr = '';
    if (headers) {
        for (const [key, value] of headers.entries()) {
            headersStr += `${key}: ${value}\n`;
        }
    }
    
    let formattedBody = bodyText;
    try {
        const json = JSON.parse(bodyText);
        formattedBody = JSON.stringify(json, null, 2);
    } catch(e) {
        // Not a JSON
    }
    
    resTextEl.innerHTML = `Status: <span class="${statusClass}">${status} ${statusText}</span>\n\n[Response Headers]\n${headersStr || '(Nenhum)'}\n\n[Response Body]\n${formattedBody || '(Sem conteúdo)'}`;
    
    // Expand console automatically on requests
    const footer = document.querySelector('.api-inspector-footer');
    if (footer.classList.contains('collapsed')) {
        // optional: auto expand
    }
}

// Ping API to check connection status
async function pingAPI() {
    const statusBadge = document.getElementById('api-status-badge');
    try {
        // Faz um fetch leve no endpoint público v1/auth/keys
        const versionPath = document.getElementById('version-v2').checked ? '/v2' : '/v1';
        const response = await fetch(`${API_BASE_URL}${versionPath}/auth/keys`);
        if (response.ok || response.status === 401 || response.status === 403 || response.status === 404) {
            statusBadge.textContent = 'Online';
            statusBadge.className = 'badge badge-online';
            return true;
        }
    } catch (error) {
        statusBadge.textContent = 'Offline';
        statusBadge.className = 'badge badge-offline';
        return false;
    }
}

// Wrapper Centralizado de Requisições HTTP
async function apiCall(endpoint, method = 'GET', body = null, customHeaders = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    // Carrega a API Key atual
    state.apiKey = document.getElementById('input-api-key').value;
    localStorage.setItem('bellaroza_api_key', state.apiKey);
    
    const headers = {
        'Content-Type': 'application/json',
        'X-API-Key': state.apiKey,
        ...customHeaders
    };

    // Garante que todo POST tenha uma chave de idempotência
    if (method.toUpperCase() === 'POST' && !headers['X-Idempotency-Key']) {
        headers['X-Idempotency-Key'] = generateUUID();
    }
    
    let fetchOptions = {
        method,
        headers
    };
    
    if (body) {
        fetchOptions.body = JSON.stringify(body);
    }
    
    // Log da Requisição no Inspector
    logAPIRequest(method, endpoint, headers, body);
    
    try {
        const response = await fetch(url, fetchOptions);
        const responseText = await response.text();
        
        // Log da Resposta no Inspector
        logAPIResponse(response.status, response.statusText, response.headers, responseText);
        
        // Tratamento de Rate Limiting
        if (response.status === 429) {
            const retryAfter = response.headers.get('Retry-After') || 'alguns segundos';
            showToast('Rate Limit Atingido!', `Muitas requisições. Tente novamente após ${retryAfter} segundos.`, 'warning');
            throw new Error(`Rate limit (429). Retry after: ${retryAfter}`);
        }
        
        if (response.status === 401) {
            showToast('Não Autorizado', 'Chave de API inválida ou ausente!', 'error');
            throw new Error('Chave de API inválida.');
        }
        
        if (!response.ok) {
            let errorMsg = 'Ocorreu um erro no processamento.';
            try {
                const errObj = JSON.parse(responseText);
                errorMsg = errObj.detail || errObj.message || errorMsg;
            } catch(e) {}
            showToast(`Erro ${response.status}`, errorMsg, 'error');
            throw new Error(errorMsg);
        }
        
        // Retorna JSON se houver
        if (responseText) {
            return JSON.parse(responseText);
        }
        return null;
        
    } catch (error) {
        console.error('API Call Failed:', error);
        pingAPI();
        throw error;
    }
}

// Inicializa Eventos da Interface
function initEventListeners() {
    // Navegação de Abas
    document.querySelectorAll('.menu-item').forEach(button => {
        button.addEventListener('click', () => {
            const tabId = button.getAttribute('data-tab');
            switchTab(tabId);
        });
    });

    // Toggle do Console Inspector
    const inspectorHeader = document.getElementById('inspector-toggle');
    const inspectorFooter = document.querySelector('.api-inspector-footer');
    inspectorHeader.addEventListener('click', () => {
        inspectorFooter.classList.toggle('collapsed');
    });
    
    // Limpar console
    document.getElementById('btn-clear-inspector').addEventListener('click', (e) => {
        e.stopPropagation(); // Evita recolher o painel ao clicar no botão
        document.getElementById('inspector-req-text').textContent = 'Console limpo. Nenhuma requisição enviada.';
        document.getElementById('inspector-res-text').textContent = 'Aguardando novas ações...';
    });
    
    // Testar conexão
    document.getElementById('btn-ping-api').addEventListener('click', async () => {
        const isOnline = await pingAPI();
        if (isOnline) {
            showToast('API Online', 'Conexão restabelecida com sucesso!');
        } else {
            showToast('API Inacessível', 'A API no servidor local não responde.', 'error');
        }
    });

    // Modais - Close generic
    document.querySelectorAll('.close-btn, .btn-secondary').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const modal = e.target.closest('.modal');
            if (modal) modal.style.display = 'none';
        });
    });

    // Modais - Fechar ao clicar fora
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.style.display = 'none';
        }
    });

    // Clientes - Ações
    document.getElementById('btn-new-client').addEventListener('click', () => {
        document.getElementById('modal-client-title').textContent = 'Novo Cliente';
        document.getElementById('form-client').reset();
        document.getElementById('input-client-id').value = '';
        
        // Habilita/Gera chave de idempotência para novo cadastro
        document.getElementById('client-idempotency-group').style.display = 'block';
        document.getElementById('client-idempotency-key').value = generateUUID();
        
        document.getElementById('modal-client').style.display = 'flex';
    });

    document.getElementById('form-client').addEventListener('submit', handleClientSubmit);
    
    document.getElementById('btn-search-client').addEventListener('click', () => {
        state.pagination.clientes.page = 0;
        state.pagination.clientes.currentSearch = document.getElementById('input-search-client').value.trim();
        loadClientes();
    });
    
    document.getElementById('input-search-client').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            state.pagination.clientes.page = 0;
            state.pagination.clientes.currentSearch = e.target.value.trim();
            loadClientes();
        }
    });

    document.getElementById('btn-prev-clientes').addEventListener('click', () => {
        if (state.pagination.clientes.page > 0) {
            state.pagination.clientes.page--;
            loadClientes();
        }
    });
    
    document.getElementById('btn-next-clientes').addEventListener('click', () => {
        if (state.pagination.clientes.page < state.pagination.clientes.totalPages - 1) {
            state.pagination.clientes.page++;
            loadClientes();
        }
    });

    // Endereços - Ações
    document.getElementById('btn-new-address').addEventListener('click', () => {
        if (!state.selectedClientId) return;
        document.getElementById('modal-address-title').textContent = 'Vincular Novo Endereço';
        document.getElementById('form-address').reset();
        document.getElementById('input-address-id').value = '';
        document.getElementById('input-address-client-id').value = state.selectedClientId;
        document.getElementById('input-address-client-name').value = state.selectedClientName;
        document.getElementById('modal-address').style.display = 'flex';
    });

    document.getElementById('form-address').addEventListener('submit', handleAddressSubmit);

    // Produtos - Ações
    document.getElementById('btn-new-product').addEventListener('click', () => {
        document.getElementById('modal-product-title').textContent = 'Novo Item do Cardápio';
        document.getElementById('form-product').reset();
        document.getElementById('input-product-id').value = '';
        document.getElementById('modal-product').style.display = 'flex';
    });

    document.getElementById('form-product').addEventListener('submit', handleProductSubmit);
    
    document.getElementById('select-filter-tamanho').addEventListener('change', (e) => {
        state.pagination.produtos.page = 0;
        state.pagination.produtos.currentSize = e.target.value;
        loadProdutos();
    });

    document.querySelectorAll('input[name="api-version"]').forEach(radio => {
        radio.addEventListener('change', () => {
            state.pagination.produtos.page = 0;
            loadProdutos();
        });
    });

    document.getElementById('btn-prev-produtos').addEventListener('click', () => {
        if (state.pagination.produtos.page > 0) {
            state.pagination.produtos.page--;
            loadProdutos();
        }
    });
    
    document.getElementById('btn-next-produtos').addEventListener('click', () => {
        if (state.pagination.produtos.page < state.pagination.produtos.totalPages - 1) {
            state.pagination.produtos.page++;
            loadProdutos();
        }
    });

    // Ingredientes - Ações
    document.getElementById('btn-new-ingredient').addEventListener('click', () => {
        document.getElementById('modal-ingredient-title').textContent = 'Novo Ingrediente';
        document.getElementById('form-ingredient').reset();
        document.getElementById('input-ingredient-id').value = '';
        document.getElementById('modal-ingredient').style.display = 'flex';
    });

    document.getElementById('form-ingredient').addEventListener('submit', handleIngredientSubmit);

    document.getElementById('btn-search-ingredient').addEventListener('click', () => {
        state.pagination.ingredientes.page = 0;
        state.pagination.ingredientes.currentSearch = document.getElementById('input-search-ingredient').value.trim();
        loadIngredientes();
    });
    
    document.getElementById('input-search-ingredient').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            state.pagination.ingredientes.page = 0;
            state.pagination.ingredientes.currentSearch = e.target.value.trim();
            loadIngredientes();
        }
    });

    document.getElementById('btn-prev-ingredientes').addEventListener('click', () => {
        if (state.pagination.ingredientes.page > 0) {
            state.pagination.ingredientes.page--;
            loadIngredientes();
        }
    });
    
    document.getElementById('btn-next-ingredientes').addEventListener('click', () => {
        if (state.pagination.ingredientes.page < state.pagination.ingredientes.totalPages - 1) {
            state.pagination.ingredientes.page++;
            loadIngredientes();
        }
    });

    // API Key Modal Trigger
    document.getElementById('btn-generate-key-modal').addEventListener('click', () => {
        document.getElementById('modal-api-key').style.display = 'flex';
        loadKeysList();
    });

    document.getElementById('form-api-key').addEventListener('submit', handleGenerateApiKey);

    // Carrinho / Novo Pedido
    document.getElementById('btn-submit-order').addEventListener('click', handleSubmitOrder);

    // Pedidos - Filtros e Paginação
    document.getElementById('select-filter-pedido-status').addEventListener('change', (e) => {
        state.pagination.pedidos.page = 0;
        state.pagination.pedidos.currentStatus = e.target.value;
        loadPedidos();
    });

    document.getElementById('btn-prev-pedidos').addEventListener('click', () => {
        if (state.pagination.pedidos.page > 0) {
            state.pagination.pedidos.page--;
            loadPedidos();
        }
    });
    
    document.getElementById('btn-next-pedidos').addEventListener('click', () => {
        if (state.pagination.pedidos.page < state.pagination.pedidos.totalPages - 1) {
            state.pagination.pedidos.page++;
            loadPedidos();
        }
    });
}

// Navegação entre abas
function switchTab(tabId) {
    // Remove classe ativa de todos
    document.querySelectorAll('.menu-item').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    
    // Adiciona classe ativa na aba correta
    const selectedBtn = document.querySelector(`.menu-item[data-tab="${tabId}"]`);
    if (selectedBtn) selectedBtn.classList.add('active');
    
    const selectedContent = document.getElementById(tabId);
    if (selectedContent) selectedContent.classList.add('active');
    
    // Atualiza título do Header
    const tabTitles = {
        'tab-dashboard': 'Painel Geral',
        'tab-clientes': 'Clientes & Endereços',
        'tab-produtos': 'Cardápio (Produtos)',
        'tab-ingredientes': 'Ingredientes',
        'tab-carrinho': 'Novo Pedido',
        'tab-pedidos': 'Pedidos Realizados'
    };
    document.getElementById('current-tab-title').textContent = tabTitles[tabId] || 'Pizzaria';
    
    state.activeTab = tabId;
    
    // Carrega dados da aba
    if (tabId === 'tab-dashboard') loadDashboardStats();
    if (tabId === 'tab-clientes') loadClientes();
    if (tabId === 'tab-produtos') loadProdutos();
    if (tabId === 'tab-ingredientes') loadIngredientes();
    if (tabId === 'tab-carrinho') loadCartInterface();
    if (tabId === 'tab-pedidos') loadPedidos();
}

// Carregar estatísticas do Dashboard
async function loadDashboardStats() {
    try {
        state.apiKey = document.getElementById('input-api-key').value;
        
        // Requisições paralelas simuladas ou sucessivas
        const versionPrefix = document.getElementById('version-v2').checked ? '/v2' : '/v1';
        const clientsRes = await apiCall(`${versionPrefix}/clientes?size=1`);
        const productsRes = await apiCall(`${versionPrefix}/produtos?size=1`, 'GET');
        const ingredientsRes = await apiCall(`${versionPrefix}/ingredientes?size=1`);
        const ordersRes = await apiCall(`${versionPrefix}/pedidos?size=1`);
        
        document.getElementById('stat-clientes-count').textContent = clientsRes.totalElements ?? 0;
        document.getElementById('stat-produtos-count').textContent = productsRes.totalElements ?? 0;
        document.getElementById('stat-ingredientes-count').textContent = ingredientsRes.totalElements ?? 0;
        document.getElementById('stat-pedidos-count').textContent = ordersRes.totalElements ?? 0;
    } catch(err) {
        document.getElementById('stat-clientes-count').textContent = 'Erro';
        document.getElementById('stat-produtos-count').textContent = 'Erro';
        document.getElementById('stat-ingredientes-count').textContent = 'Erro';
        document.getElementById('stat-pedidos-count').textContent = 'Erro';
    }
}

// ==========================================
// FLUXOS DE CLIENTES & ENDEREÇOS
// ==========================================

async function loadClientes() {
    const { page, size, currentSearch } = state.pagination.clientes;
    const versionPrefix = document.getElementById('version-v2').checked ? '/v2' : '/v1';
    let endpoint = `${versionPrefix}/clientes?page=${page}&size=${size}`;
    
    if (currentSearch) {
        endpoint = `${versionPrefix}/clientes/busca?nome=${encodeURIComponent(currentSearch)}&page=${page}&size=${size}`;
    }
    
    try {
        const data = await apiCall(endpoint);
        const tbody = document.getElementById('tbody-clientes');
        tbody.innerHTML = '';
        
        state.pagination.clientes.totalPages = data.totalPages || 1;
        document.getElementById('span-info-clientes').textContent = `Página ${(page + 1)} de ${state.pagination.clientes.totalPages}`;
        
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-muted">Nenhum cliente encontrado.</td></tr>';
            clearSelectedClient();
            return;
        }
        
        data.content.forEach(client => {
            const tr = document.createElement('tr');
            tr.className = 'clickable-row';
            if (state.selectedClientId === client.id) {
                tr.classList.add('selected-row');
            }
            
            tr.innerHTML = `
                <td><strong>${client.id}</strong></td>
                <td>${client.nome}</td>
                <td>${client.email}</td>
                <td>${client.telefone}</td>
                <td>
                    <button class="btn btn-secondary btn-xs" onclick="editCliente(event, ${client.id}, '${client.nome}', '${client.email}', '${client.telefone}')"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn btn-danger btn-xs" onclick="deleteCliente(event, ${client.id})"><i class="fa-solid fa-trash"></i></button>
                </td>
            `;
            
            tr.addEventListener('click', () => selectClient(client.id, client.nome));
            tbody.appendChild(tr);
        });
    } catch(e) {}
}

function selectClient(id, name) {
    state.selectedClientId = id;
    state.selectedClientName = name;
    
    // Atualiza destaques nas linhas
    document.querySelectorAll('#table-clientes tbody tr').forEach(row => {
        row.classList.remove('selected-row');
        if (row.cells[0].textContent.includes(id)) {
            row.classList.add('selected-row');
        }
    });

    // Configura o indicador de cliente selecionado
    const indicator = document.getElementById('selected-client-indicator');
    indicator.innerHTML = `Cliente: <strong>${name}</strong> (ID: ${id}) &bull; Endereços de entrega vinculados:`;
    
    // Habilita botão para adicionar endereço
    document.getElementById('btn-new-address').removeAttribute('disabled');
    
    // Mostra tabela de endereços e carrega
    document.getElementById('table-enderecos').classList.remove('hidden');
    loadEnderecos(id);
}

function clearSelectedClient() {
    state.selectedClientId = null;
    state.selectedClientName = '';
    document.getElementById('btn-new-address').setAttribute('disabled', 'true');
    document.getElementById('selected-client-indicator').textContent = 'Selecione um cliente na lista ao lado para gerenciar seus endereços.';
    document.getElementById('table-enderecos').classList.add('hidden');
}

async function handleClientSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('input-client-id').value;
    const nome = document.getElementById('input-client-nome').value;
    const email = document.getElementById('input-client-email').value;
    const telefone = document.getElementById('input-client-telefone').value;
    
    const payload = { nome, email, telefone };
    
    try {
        if (id) {
            // Edit
            await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/clientes/${id}`, 'PUT', payload);
            showToast('Cliente Atualizado', `Os dados de ${nome} foram salvos com sucesso.`);
        } else {
            // Create (Suporta Idempotency Key)
            const idempotencyKey = document.getElementById('client-idempotency-key').value;
            await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/clientes`, 'POST', payload, {
                'X-Idempotency-Key': idempotencyKey
            });
            showToast('Cliente Cadastrado', `Cliente ${nome} criado com sucesso.`);
            refreshIdempotencyKeys();
        }
        
        document.getElementById('modal-client').style.display = 'none';
        loadClientes();
    } catch(err) {}
}

function editCliente(e, id, nome, email, telefone) {
    e.stopPropagation(); // Impede seleção da linha
    
    document.getElementById('modal-client-title').textContent = 'Editar Cliente';
    document.getElementById('input-client-id').value = id;
    document.getElementById('input-client-nome').value = nome;
    document.getElementById('input-client-email').value = email;
    document.getElementById('input-client-telefone').value = telefone;
    
    // Oculta idempotency key para edições (PUT não exige idempotência na mesma lógica de criação no backend)
    document.getElementById('client-idempotency-group').style.display = 'none';
    
    document.getElementById('modal-client').style.display = 'flex';
}

async function deleteCliente(e, id) {
    e.stopPropagation();
    if (!confirm(`Deseja realmente remover o cliente com ID ${id}? Isso pode remover pedidos e endereços associados!`)) return;
    
    try {
        await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/clientes/${id}`, 'DELETE');
        showToast('Cliente Removido', `Cliente ID ${id} excluído com sucesso.`);
        if (state.selectedClientId === id) {
            clearSelectedClient();
        }
        loadClientes();
    } catch(err) {}
}

// ==========================================
// FLUXOS DE ENDEREÇOS
// ==========================================

async function loadEnderecos(clientId) {
    try {
        // Busca todos endereços vinculados
        // A API traz paginado. Usaremos page=0 e size=50 para carregar todos no painel
        const data = await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/enderecos?size=50`);
        const tbody = document.getElementById('tbody-enderecos');
        tbody.innerHTML = '';
        
        // Filtra localmente os endereços que pertencem a este cliente
        // (Já que a API não possui um endpoint específico GET /clientes/{id}/enderecos direto)
        const clienteEnderecos = data.content.filter(end => end.clienteId === clientId);
        
        if (clienteEnderecos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center">Nenhum endereço cadastrado para este cliente.</td></tr>';
            return;
        }
        
        clienteEnderecos.forEach(end => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${end.rua}</td>
                <td>${end.numero}</td>
                <td>${end.bairro || '-'}</td>
                <td>${end.cep}</td>
                <td>
                    <button class="btn btn-secondary btn-xs" onclick="editEndereco(event, ${end.id}, '${end.rua}', '${end.numero}', '${end.bairro || ''}', '${end.cep}', ${end.clienteId})"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn btn-danger btn-xs" onclick="deleteEndereco(event, ${end.id})"><i class="fa-solid fa-trash"></i></button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch(e) {}
}

async function handleAddressSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('input-address-id').value;
    const clienteId = document.getElementById('input-address-client-id').value;
    const rua = document.getElementById('input-address-rua').value;
    const numero = document.getElementById('input-address-numero').value;
    const bairro = document.getElementById('input-address-bairro').value;
    const cep = document.getElementById('input-address-cep').value;
    
    const payload = { rua, numero, bairro, cep, clienteId: parseInt(clienteId) };
    
    try {
        if (id) {
            await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/enderecos/${id}`, 'PUT', payload);
            showToast('Endereço Atualizado', 'Modificações salvas com sucesso.');
        } else {
            await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/enderecos`, 'POST', payload);
            showToast('Endereço Adicionado', 'Endereço vinculado ao cliente com sucesso.');
        }
        
        document.getElementById('modal-address').style.display = 'none';
        loadEnderecos(parseInt(clienteId));
    } catch(err) {}
}

function editEndereco(e, id, rua, numero, bairro, cep, clienteId) {
    e.stopPropagation();
    document.getElementById('modal-address-title').textContent = 'Editar Endereço';
    document.getElementById('input-address-id').value = id;
    document.getElementById('input-address-client-id').value = clienteId;
    document.getElementById('input-address-client-name').value = state.selectedClientName;
    document.getElementById('input-address-cep').value = cep;
    document.getElementById('input-address-rua').value = rua;
    document.getElementById('input-address-numero').value = numero;
    document.getElementById('input-address-bairro').value = bairro;
    
    document.getElementById('modal-address').style.display = 'flex';
}

async function deleteEndereco(e, id) {
    e.stopPropagation();
    if (!confirm(`Deseja realmente desvincular o endereço ID ${id}?`)) return;
    
    try {
        await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/enderecos/${id}`, 'DELETE');
        showToast('Endereço Removido', 'Endereço excluído com sucesso.');
        loadEnderecos(state.selectedClientId);
    } catch(err) {}
}

// ==========================================
// FLUXOS DE PRODUTOS (CARDÁPIO)
// ==========================================

async function loadProdutos() {
    const { page, size, currentSize } = state.pagination.produtos;
    const isV2 = document.getElementById('version-v2').checked;
    const versionPath = isV2 ? '2' : '1';
    
    let endpoint = `/v${versionPath}/produtos?page=${page}&size=${size}`;
    try {
        const data = await apiCall(endpoint, 'GET');
        const tbody = document.getElementById('tbody-produtos');
        tbody.innerHTML = '';
        
        state.pagination.produtos.totalPages = data.totalPages || 1;
        document.getElementById('span-info-produtos').textContent = `Página ${(page + 1)} de ${state.pagination.produtos.totalPages}`;
        
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center">Nenhum produto cadastrado no cardápio.</td></tr>';
            return;
        }
        
        data.content.forEach(prod => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${prod.id}</strong></td>
                <td><strong>${prod.nome}</strong></td>
                <td>${prod.descricao || '-'}</td>
                <td><span class="prod-size-badge">${prod.tamanho}</span></td>
                <td class="prod-price">R$ ${prod.precoBase.toFixed(2)}</td>
                <td>
                    <button class="btn btn-secondary btn-xs" onclick="editProduto(event, ${prod.id}, '${prod.nome}', '${prod.descricao || ''}', '${prod.tamanho}', ${prod.precoBase})"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn btn-danger btn-xs" onclick="deleteProduto(event, ${prod.id})"><i class="fa-solid fa-trash"></i></button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch(e) {}
}

async function handleProductSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('input-product-id').value;
    const nome = document.getElementById('input-product-nome').value;
    const descricao = document.getElementById('input-product-descricao').value;
    const tamanho = document.getElementById('select-product-tamanho').value;
    const precoBase = parseFloat(document.getElementById('input-product-preco').value);
    
    const payload = { nome, descricao, tamanho, precoBase };
    const versionHeader = document.getElementById('version-v2').checked ? '2' : '1';
    
    try {
        if (id) {
            await apiCall(`/v${versionHeader}/produtos/${id}`, 'PUT', payload);
            showToast('Produto Atualizado', `${nome} atualizado no cardápio.`);
        } else {
            await apiCall(`/v${versionHeader}/produtos`, 'POST', payload);
            showToast('Produto Adicionado', `${nome} inserido com sucesso.`);
        }
        document.getElementById('modal-product').style.display = 'none';
        loadProdutos();
    } catch(err) {}
}

function editProduto(e, id, nome, descricao, tamanho, precoBase) {
    e.stopPropagation();
    
    document.getElementById('modal-product-title').textContent = 'Editar Produto';
    document.getElementById('input-product-id').value = id;
    document.getElementById('input-product-nome').value = nome;
    document.getElementById('input-product-descricao').value = descricao;
    document.getElementById('select-product-tamanho').value = tamanho;
    document.getElementById('input-product-preco').value = precoBase;
    
    document.getElementById('modal-product').style.display = 'flex';
}

async function deleteProduto(e, id) {
    e.stopPropagation();
    if (!confirm(`Remover o produto ID ${id} do cardápio?`)) return;
    
    const versionHeader = document.getElementById('version-v2').checked ? '2' : '1';
    try {
        await apiCall(`/v${versionHeader}/produtos/${id}`, 'DELETE');
        showToast('Produto Removido', 'Item excluído do cardápio.');
        loadProdutos();
    } catch(err) {}
}

// ==========================================
// FLUXOS DE INGREDIENTES
// ==========================================

async function loadIngredienteOptionsForSelector() {
    // Traz a lista simples de ingredientes para renderizar futuramente caso precise vinculá-los.
}

async function loadIngredientes() {
    const { page, size, currentSearch } = state.pagination.ingredientes;
    const versionPrefix = document.getElementById('version-v2').checked ? '/v2' : '/v1';
    let endpoint = `${versionPrefix}/ingredientes?page=${page}&size=${size}`;
    
    if (currentSearch) {
        endpoint = `${versionPrefix}/ingredientes/busca?nome=${encodeURIComponent(currentSearch)}&page=${page}&size=${size}`;
    }
    
    try {
        const data = await apiCall(endpoint);
        const tbody = document.getElementById('tbody-ingredientes');
        tbody.innerHTML = '';
        
        state.pagination.ingredientes.totalPages = data.totalPages || 1;
        document.getElementById('span-info-ingredientes').textContent = `Página ${(page + 1)} de ${state.pagination.ingredientes.totalPages}`;
        
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-muted text-center">Nenhum ingrediente catalogado.</td></tr>';
            return;
        }
        
        data.content.forEach(ing => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${ing.id}</strong></td>
                <td><strong>${ing.nome}</strong></td>
                <td>${ing.descricao || '-'}</td>
                <td>
                    <button class="btn btn-secondary btn-xs" onclick="editIngrediente(event, ${ing.id}, '${ing.nome}', '${ing.descricao || ''}')"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn btn-danger btn-xs" onclick="deleteIngrediente(event, ${ing.id})"><i class="fa-solid fa-trash"></i></button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch(e) {}
}

async function handleIngredientSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('input-ingredient-id').value;
    const nome = document.getElementById('input-ingredient-nome').value;
    const descricao = document.getElementById('input-ingredient-descricao').value;
    
    const payload = { nome, descricao };
    
    try {
        if (id) {
            await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/ingredientes/${id}`, 'PUT', payload);
            showToast('Ingrediente Atualizado', 'Alterações salvas.');
        } else {
            await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/ingredientes`, 'POST', payload);
            showToast('Ingrediente Cadastrado', `${nome} adicionado ao catálogo.`);
        }
        document.getElementById('modal-ingredient').style.display = 'none';
        loadIngredientes();
    } catch(err) {}
}

function editIngrediente(e, id, nome, descricao) {
    e.stopPropagation();
    
    document.getElementById('modal-ingredient-title').textContent = 'Editar Ingrediente';
    document.getElementById('input-ingredient-id').value = id;
    document.getElementById('input-ingredient-nome').value = nome;
    document.getElementById('input-ingredient-descricao').value = descricao;
    
    document.getElementById('modal-ingredient').style.display = 'flex';
}

async function deleteIngrediente(e, id) {
    e.stopPropagation();
    if (!confirm(`Remover ingrediente ID ${id}?`)) return;
    
    try {
        await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/ingredientes/${id}`, 'DELETE');
        showToast('Ingrediente Excluído', 'Ingrediente removido com sucesso.');
        loadIngredientes();
    } catch(err) {}
}

// ==========================================
// FLUXO DE COMPRAS (CARRINHO & PEDIDO)
// ==========================================

async function loadCartInterface() {
    // 1. Carregar Clientes para o Dropdown
    try {
        const clientsRes = await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/clientes?size=100`);
        const select = document.getElementById('cart-select-cliente');
        select.innerHTML = '<option value="">-- Selecione o Cliente --</option>';
        
        if (clientsRes.content) {
            clientsRes.content.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = `${c.nome} (ID: ${c.id})`;
                select.appendChild(opt);
            });
        }
    } catch(err) {}

    // 2. Carregar Produtos para o Grid (V1 padrão)
    try {
        const prodRes = await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/produtos?size=100`, 'GET');
        const grid = document.getElementById('cart-products-list');
        grid.innerHTML = '';
        
        state.productsList = prodRes.content || [];
        
        if (state.productsList.length === 0) {
            grid.innerHTML = '<p class="text-muted">Nenhum produto ativo no cardápio.</p>';
            return;
        }
        
        state.productsList.forEach(p => {
            const card = document.createElement('div');
            card.className = 'product-cart-card';
            card.innerHTML = `
                <div>
                    <div class="prod-title">${p.nome}</div>
                    <div class="prod-desc">${p.descricao || 'Sem descrição.'}</div>
                </div>
                <div class="prod-footer">
                    <div>
                        <span class="prod-price">R$ ${p.precoBase.toFixed(2)}</span>
                        <div class="prod-size-badge" style="margin-top: 2px;">${p.tamanho}</div>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="addToCart(${p.id})"><i class="fa-solid fa-plus"></i></button>
                </div>
            `;
            grid.appendChild(card);
        });
    } catch(err) {}
}

function addToCart(productId) {
    const product = state.productsList.find(p => p.id === productId);
    if (!product) return;
    
    const existing = state.cart.find(item => item.produtoId === productId);
    if (existing) {
        existing.quantidade++;
    } else {
        state.cart.push({
            produtoId: product.id,
            nome: product.nome,
            preco: product.precoBase,
            quantidade: 1
        });
    }
    
    renderCart();
}

function updateCartQty(productId, delta) {
    const item = state.cart.find(i => i.produtoId === productId);
    if (!item) return;
    
    item.quantidade += delta;
    if (item.quantidade <= 0) {
        state.cart = state.cart.filter(i => i.produtoId !== productId);
    }
    
    renderCart();
}

function renderCart() {
    const container = document.getElementById('cart-items-container');
    container.innerHTML = '';
    
    if (state.cart.length === 0) {
        container.innerHTML = '<li class="empty-cart-msg">Carrinho vazio. Adicione itens clicando em "+" ao lado.</li>';
        document.getElementById('cart-total-value').textContent = 'R$ 0,00';
        return;
    }
    
    let total = 0;
    state.cart.forEach(item => {
        const itemTotal = item.preco * item.quantidade;
        total += itemTotal;
        
        const li = document.createElement('li');
        li.className = 'cart-item';
        li.innerHTML = `
            <div class="cart-item-name">${item.nome}</div>
            <div class="cart-item-price">R$ ${itemTotal.toFixed(2)}</div>
            <div class="cart-item-controls">
                <button class="btn btn-secondary btn-xs" onclick="updateCartQty(${item.produtoId}, -1)"><i class="fa-solid fa-minus"></i></button>
                <span class="cart-item-qty">${item.quantidade}</span>
                <button class="btn btn-secondary btn-xs" onclick="updateCartQty(${item.produtoId}, 1)"><i class="fa-solid fa-plus"></i></button>
            </div>
        `;
        container.appendChild(li);
    });
    
    document.getElementById('cart-total-value').textContent = `R$ ${total.toFixed(2)}`;
}

async function handleSubmitOrder() {
    const clienteId = document.getElementById('cart-select-cliente').value;
    if (!clienteId) {
        showToast('Campos Obrigatórios', 'Você deve selecionar um cliente.', 'warning');
        return;
    }
    if (state.cart.length === 0) {
        showToast('Carrinho Vazio', 'Adicione pelo menos um produto ao carrinho.', 'warning');
        return;
    }
    
    const itemsPayload = state.cart.map(i => ({
        produtoId: i.produtoId,
        quantidade: i.quantidade
    }));
    
    const payload = {
        clienteId: parseInt(clienteId),
        itens: itemsPayload
    };
    
    const idempotencyKey = document.getElementById('cart-idempotency-key').value;
    
    try {
        const orderCreated = await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/pedidos`, 'POST', payload, {
            'X-Idempotency-Key': idempotencyKey
        });
        
        showToast('Pedido Confirmado!', `Pedido #${orderCreated.id} gerado com sucesso.`);
        
        // Limpa estado do carrinho
        state.cart = [];
        document.getElementById('cart-select-cliente').value = '';
        renderCart();
        
        // Regenera chaves de idempotência para o próximo pedido
        refreshIdempotencyKeys();
        
        // Redireciona para listagem de pedidos
        switchTab('tab-pedidos');
    } catch(err) {}
}

// ==========================================
// FLUXO DE PEDIDOS REALIZADOS
// ==========================================

async function loadPedidos() {
    const { page, size, currentStatus } = state.pagination.pedidos;
    const versionPrefix = document.getElementById('version-v2').checked ? '/v2' : '/v1';
    let endpoint = `${versionPrefix}/pedidos?page=${page}&size=${size}`;
    
    if (currentStatus) {
        endpoint = `${versionPrefix}/pedidos/status?status=${currentStatus}&page=${page}&size=${size}`;
    }
    
    try {
        const data = await apiCall(endpoint);
        const tbody = document.getElementById('tbody-pedidos');
        tbody.innerHTML = '';
        
        state.pagination.pedidos.totalPages = data.totalPages || 1;
        document.getElementById('span-info-pedidos').textContent = `Página ${(page + 1)} de ${state.pagination.pedidos.totalPages}`;
        
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-muted text-center">Nenhum pedido encontrado.</td></tr>';
            return;
        }
        
        data.content.forEach(p => {
            const tr = document.createElement('tr');
            
            // Formatar Data
            const dateStr = p.dataHora ? new Date(p.dataHora).toLocaleString('pt-BR') : '-';
            
            // Formatar Itens
            let itemsText = '';
            let total = 0;
            if (p.itens && p.itens.length > 0) {
                itemsText = p.itens.map(item => {
                    total += item.precoUnitario * item.quantidade;
                    return `${item.quantidade}x ${item.produtoNome}`;
                }).join(', ');
            } else {
                itemsText = '(Sem itens)';
            }
            
            // Classe de status do pedido
            const statusClass = `badge-status-${p.status.toLowerCase()}`;
            
            // Botões rápidos de controle de status
            const statusFlow = ['RECEBIDO', 'PREPARANDO', 'PRONTO', 'ENTREGUE'];
            const currentIndex = statusFlow.indexOf(p.status);
            let flowButtonHTML = '';
            if (currentIndex !== -1 && currentIndex < statusFlow.length - 1) {
                const nextStatus = statusFlow[currentIndex + 1];
                flowButtonHTML = `<button class="btn btn-warning btn-xs" onclick="updatePedidoStatus(${p.id}, '${nextStatus}')">Mudar para ${nextStatus}</button>`;
            } else {
                flowButtonHTML = `<span class="text-muted" style="font-size:0.75rem;"><i class="fa-solid fa-circle-check text-success"></i> Concluído</span>`;
            }
            
            tr.innerHTML = `
                <td><strong>#${p.id}</strong></td>
                <td>${dateStr}</td>
                <td>Cliente ID: ${p.clienteId}</td>
                <td style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${itemsText}">${itemsText}</td>
                <td class="prod-price">R$ ${total.toFixed(2)}</td>
                <td><span class="badge ${statusClass}">${p.status}</span></td>
                <td>${flowButtonHTML}</td>
                <td>
                    <button class="btn btn-danger btn-xs" onclick="cancelarPedido(${p.id})"><i class="fa-solid fa-xmark"></i></button>
                </td>
            `;
            
            tbody.appendChild(tr);
        });
    } catch(e) {}
}

async function updatePedidoStatus(id, newStatus) {
    try {
        await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/pedidos/${id}/status?status=${newStatus}`, 'PUT');
        showToast('Status Atualizado', `Pedido #${id} avançou para ${newStatus}.`);
        loadPedidos();
    } catch(err) {}
}

async function cancelarPedido(id) {
    if (!confirm(`Tem certeza que deseja cancelar e excluir o pedido #${id}?`)) return;
    
    try {
        await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/pedidos/${id}`, 'DELETE');
        showToast('Pedido Cancelado', `Pedido #${id} removido com sucesso.`);
        loadPedidos();
    } catch(err) {}
}

// ==========================================
// CONTROLE DE API KEYS
// ==========================================

async function loadKeysList() {
    try {
        const keys = await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/auth/keys`);
        const tbody = document.getElementById('tbody-keys-list');
        tbody.innerHTML = '';
        
        if (!keys || keys.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-muted text-center">Nenhuma chave gerada.</td></tr>';
            return;
        }
        
        keys.forEach(k => {
            const tr = document.createElement('tr');
            
            const dateStr = k.dataCriacao ? new Date(k.dataCriacao).toLocaleDateString('pt-BR') : '-';
            
            tr.innerHTML = `
                <td>${k.dono}</td>
                <td><code>${k.chave}</code></td>
                <td>${dateStr}</td>
                <td>
                    <button type="button" class="btn btn-secondary btn-xs" onclick="useApiKey('${k.chave}')">Usar</button>
                </td>
            `;
            
            tbody.appendChild(tr);
        });
    } catch(e) {}
}

async function handleGenerateApiKey(e) {
    e.preventDefault();
    const dono = document.getElementById('input-api-key-dono').value;
    
    try {
        const keyData = await apiCall(`${document.getElementById('version-v2').checked ? '/v2' : '/v1'}/auth/keys?dono=${encodeURIComponent(dono)}`, 'POST');
        showToast('API Key Gerada', `Chave criada para o dono: ${dono}`);
        
        // Aplica a nova chave gerada automaticamente
        useApiKey(keyData.chave);
        
        document.getElementById('input-api-key-dono').value = '';
        loadKeysList();
    } catch(err) {}
}

function useApiKey(key) {
    state.apiKey = key;
    document.getElementById('input-api-key').value = key;
    localStorage.setItem('bellaroza_api_key', key);
    showToast('Chave em Uso', 'Nova X-API-Key configurada para as requisições.');
    
    // Recarrega estatísticas do dashboard para testar autenticidade
    if (state.activeTab === 'tab-dashboard') {
        loadDashboardStats();
    }
}
