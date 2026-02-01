# Configuração do Winlator Hub (Supabase)

Este projeto utiliza o Supabase para gerenciar repositórios, categorias e configurações de jogos de forma dinâmica.

## 1. Configuração do Banco de Dados
Para que o app funcione corretamente, você deve executar os comandos SQL contidos no arquivo `supabase_setup.sql` no **SQL Editor** do seu painel do Supabase. Isso criará as tabelas e as políticas de segurança (RLS).

## 2. Acesso ao Painel Administrativo (Winlator Panel)
O app de painel exige login para garantir que apenas você possa editar os dados.

### Como criar seu usuário administrador:
1. Vá para o Dashboard do Supabase.
2. Entre em **Authentication** -> **Users**.
3. Clique em **Add User** -> **Create new user**.
4. Defina um e-mail e senha.
5. Use esses dados para entrar no app **Winlator Hub Panel**.

## 3. Segurança (RLS)
As políticas de segurança configuradas permitem:
- **Público (Hub):** Ver categorias, repositórios e configurações de jogos aprovadas.
- **Público (Hub):** Sugerir novas configurações de jogos (ficam como 'pending').
- **Autenticado (Panel):** Criar, editar e excluir qualquer dado (repositórios, categorias, configurações e avisos).
