-- Tabela de categorias
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

-- Tabela de repositórios (atualizada com category_id)
CREATE TABLE repositories (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    owner TEXT NOT NULL,
    repo TEXT NOT NULL,
    description TEXT,
    category_id INT REFERENCES categories(id) ON DELETE SET NULL
);

-- Tabela de configuração do app
CREATE TABLE app_config (
    id INT PRIMARY KEY DEFAULT 1,
    dialog_title TEXT,
    dialog_message TEXT,
    show_dialog BOOLEAN DEFAULT FALSE
);

-- Tabela para configurações de jogos (atualizada com campos de versão do winlator)
CREATE TABLE game_settings (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    format TEXT DEFAULT 'Pré instalado',
    device TEXT,
    gamepad TEXT DEFAULT 'Não',
    winlator_version TEXT, -- Descrição textual
    winlator_repo_owner TEXT, -- Para download automático
    winlator_repo_name TEXT,  -- Para download automático
    winlator_tag_name TEXT,   -- Para download automático
    winlator_asset_name TEXT, -- Para download automático
    winlator_download_url TEXT, -- URL direta se disponível
    wine_repo_owner TEXT,
    wine_repo_name TEXT,
    wine_tag_name TEXT,
    wine_asset_name TEXT,
    box64_repo_owner TEXT,
    box64_repo_name TEXT,
    box64_tag_name TEXT,
    box64_asset_name TEXT,
    gpu_driver_repo_owner TEXT,
    gpu_driver_repo_name TEXT,
    gpu_driver_tag_name TEXT,
    gpu_driver_asset_name TEXT,
    dxvk_repo_owner TEXT,
    dxvk_repo_name TEXT,
    dxvk_tag_name TEXT,
    dxvk_asset_name TEXT,
    graphics TEXT,
    wine TEXT,
    box64 TEXT,
    box64_preset TEXT,
    resolution TEXT,
    gpu_driver TEXT,
    dxvk TEXT,
    audio_driver TEXT DEFAULT 'alsa',
    submitted_by TEXT,
    youtube_url TEXT,
    status TEXT DEFAULT 'pending' -- pending, approved, rejected
);

-- Inserir categorias padrão
INSERT INTO categories (name) VALUES ('Winlator'), ('Drivers'), ('Ferramentas'), ('DXVK');

-- Inserir uma configuração padrão
INSERT INTO app_config (id, dialog_title, dialog_message, show_dialog)
VALUES (1, 'Bem-vindo', 'Este é o Winlator Hub oficial!', true)
ON CONFLICT (id) DO NOTHING;

-- SEGURANÇA: Configurar RLS (Row Level Security)
-- Isso garante que apenas usuários autenticados possam EDITAR os dados.

-- Habilitar RLS em todas as tabelas
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE repositories ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE game_settings ENABLE ROW LEVEL SECURITY;

-- Políticas para 'categories'
CREATE POLICY "Allow public read on categories" ON categories FOR SELECT USING (true);
CREATE POLICY "Allow authenticated insert on categories" ON categories FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Allow authenticated update on categories" ON categories FOR UPDATE USING (auth.role() = 'authenticated');
CREATE POLICY "Allow authenticated delete on categories" ON categories FOR DELETE USING (auth.role() = 'authenticated');

-- Políticas para 'repositories'
CREATE POLICY "Allow public read on repositories" ON repositories FOR SELECT USING (true);
CREATE POLICY "Allow authenticated insert on repositories" ON repositories FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Allow authenticated update on repositories" ON repositories FOR UPDATE USING (auth.role() = 'authenticated');
CREATE POLICY "Allow authenticated delete on repositories" ON repositories FOR DELETE USING (auth.role() = 'authenticated');

-- Políticas para 'app_config'
CREATE POLICY "Allow public read on app_config" ON app_config FOR SELECT USING (true);
CREATE POLICY "Allow authenticated update on app_config" ON app_config FOR UPDATE USING (auth.role() = 'authenticated');

-- Políticas para 'game_settings'
CREATE POLICY "Allow public read on approved game_settings" ON game_settings FOR SELECT USING (status = 'approved');
CREATE POLICY "Allow public insert on game_settings" ON game_settings FOR INSERT WITH CHECK (status = 'pending');
CREATE POLICY "Allow authenticated full access on game_settings" ON game_settings FOR ALL USING (auth.role() = 'authenticated');
