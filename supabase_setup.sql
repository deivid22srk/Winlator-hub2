-- Tabela de categorias
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

-- Tabela de repositórios
CREATE TABLE IF NOT EXISTS repositories (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    owner TEXT NOT NULL,
    repo TEXT NOT NULL,
    description TEXT,
    category_id INT REFERENCES categories(id) ON DELETE SET NULL
);

-- Tabela de configuração do app
CREATE TABLE IF NOT EXISTS app_config (
    id INT PRIMARY KEY DEFAULT 1,
    dialog_title TEXT,
    dialog_message TEXT,
    show_dialog BOOLEAN DEFAULT FALSE,
    is_update BOOLEAN DEFAULT FALSE,
    update_url TEXT
);

-- Tabela para configurações de jogos
CREATE TABLE IF NOT EXISTS game_settings (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    format TEXT DEFAULT 'Pré instalado',
    device TEXT,
    gamepad TEXT DEFAULT 'Não',
    winlator_version TEXT,
    winlator_repo_owner TEXT,
    winlator_repo_name TEXT,
    winlator_tag_name TEXT,
    winlator_asset_name TEXT,
    winlator_download_url TEXT,
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
    status TEXT DEFAULT 'pending',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Inserir categorias padrão
INSERT INTO categories (name) VALUES ('Winlator'), ('Drivers'), ('Ferramentas'), ('DXVK')
ON CONFLICT (name) DO NOTHING;

-- Inserir uma configuração padrão
INSERT INTO app_config (id, dialog_title, dialog_message, show_dialog)
VALUES (1, 'Bem-vindo', 'Este é o Winlator Hub oficial!', true)
ON CONFLICT (id) DO NOTHING;

-- SEGURANÇA: Configurar RLS (Row Level Security)
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE repositories ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE game_settings ENABLE ROW LEVEL SECURITY;

-- Helper function to safely create policies
DO $$
BEGIN
    -- Categorias
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public read on categories' AND tablename = 'categories') THEN
        CREATE POLICY "Allow public read on categories" ON categories FOR SELECT USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow authenticated insert on categories' AND tablename = 'categories') THEN
        CREATE POLICY "Allow authenticated insert on categories" ON categories FOR INSERT WITH CHECK (auth.role() = 'authenticated');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow authenticated update on categories' AND tablename = 'categories') THEN
        CREATE POLICY "Allow authenticated update on categories" ON categories FOR UPDATE USING (auth.role() = 'authenticated');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow authenticated delete on categories' AND tablename = 'categories') THEN
        CREATE POLICY "Allow authenticated delete on categories" ON categories FOR DELETE USING (auth.role() = 'authenticated');
    END IF;

    -- Repositories
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public read on repositories' AND tablename = 'repositories') THEN
        CREATE POLICY "Allow public read on repositories" ON repositories FOR SELECT USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow authenticated insert on repositories' AND tablename = 'repositories') THEN
        CREATE POLICY "Allow authenticated insert on repositories" ON repositories FOR INSERT WITH CHECK (auth.role() = 'authenticated');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow authenticated update on repositories' AND tablename = 'repositories') THEN
        CREATE POLICY "Allow authenticated update on repositories" ON repositories FOR UPDATE USING (auth.role() = 'authenticated');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow authenticated delete on repositories' AND tablename = 'repositories') THEN
        CREATE POLICY "Allow authenticated delete on repositories" ON repositories FOR DELETE USING (auth.role() = 'authenticated');
    END IF;

    -- App Config
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public read on app_config' AND tablename = 'app_config') THEN
        CREATE POLICY "Allow public read on app_config" ON app_config FOR SELECT USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow authenticated update on app_config' AND tablename = 'app_config') THEN
        CREATE POLICY "Allow authenticated update on app_config" ON app_config FOR UPDATE USING (auth.role() = 'authenticated');
    END IF;

    -- Game Settings
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public read on approved game_settings' AND tablename = 'game_settings') THEN
        CREATE POLICY "Allow public read on approved game_settings" ON game_settings FOR SELECT USING (status = 'approved');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public insert on game_settings' AND tablename = 'game_settings') THEN
        CREATE POLICY "Allow public insert on game_settings" ON game_settings FOR INSERT WITH CHECK (status = 'pending');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow authenticated full access on game_settings' AND tablename = 'game_settings') THEN
        CREATE POLICY "Allow authenticated full access on game_settings" ON game_settings FOR ALL USING (auth.role() = 'authenticated');
    END IF;
END $$;
