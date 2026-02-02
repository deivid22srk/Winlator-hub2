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
    update_url TEXT,
    latest_version INT DEFAULT 1
);

-- Migrações para app_config (adicionar colunas se não existirem)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='app_config' AND column_name='latest_version') THEN
        ALTER TABLE app_config ADD COLUMN latest_version INT DEFAULT 1;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='app_config' AND column_name='is_update') THEN
        ALTER TABLE app_config ADD COLUMN is_update BOOLEAN DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='app_config' AND column_name='update_url') THEN
        ALTER TABLE app_config ADD COLUMN update_url TEXT;
    END IF;
END $$;

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
    created_at TIMESTAMPTZ DEFAULT NOW(),
    likes_count INT NOT NULL DEFAULT 0,
    dislikes_count INT NOT NULL DEFAULT 0
);

-- Migrações para game_settings
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='game_settings' AND column_name='created_at') THEN
        ALTER TABLE game_settings ADD COLUMN created_at TIMESTAMPTZ DEFAULT NOW();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='game_settings' AND column_name='likes_count') THEN
        ALTER TABLE game_settings ADD COLUMN likes_count INT NOT NULL DEFAULT 0;
    ELSE
        UPDATE game_settings SET likes_count = 0 WHERE likes_count IS NULL;
        ALTER TABLE game_settings ALTER COLUMN likes_count SET NOT NULL;
        ALTER TABLE game_settings ALTER COLUMN likes_count SET DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='game_settings' AND column_name='dislikes_count') THEN
        ALTER TABLE game_settings ADD COLUMN dislikes_count INT NOT NULL DEFAULT 0;
    ELSE
        UPDATE game_settings SET dislikes_count = 0 WHERE dislikes_count IS NULL;
        ALTER TABLE game_settings ALTER COLUMN dislikes_count SET NOT NULL;
        ALTER TABLE game_settings ALTER COLUMN dislikes_count SET DEFAULT 0;
    END IF;
END $$;

-- Tabela de votos (Like/Dislike)
CREATE TABLE IF NOT EXISTS game_settings_votes (
    game_setting_id INT REFERENCES game_settings(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL,
    vote_type INT NOT NULL, -- 1 para like, -1 para dislike
    PRIMARY KEY (game_setting_id, user_id)
);

-- Trigger function para atualizar contadores de votos
CREATE OR REPLACE FUNCTION update_game_setting_vote_counts()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        IF (NEW.vote_type = 1) THEN
            UPDATE game_settings SET likes_count = likes_count + 1 WHERE id = NEW.game_setting_id;
        ELSE
            UPDATE game_settings SET dislikes_count = dislikes_count + 1 WHERE id = NEW.game_setting_id;
        END IF;
    ELSIF (TG_OP = 'UPDATE') THEN
        IF (OLD.vote_type = 1 AND NEW.vote_type = -1) THEN
            UPDATE game_settings SET likes_count = likes_count - 1, dislikes_count = dislikes_count + 1 WHERE id = NEW.game_setting_id;
        ELSIF (OLD.vote_type = -1 AND NEW.vote_type = 1) THEN
            UPDATE game_settings SET dislikes_count = dislikes_count - 1, likes_count = likes_count + 1 WHERE id = NEW.game_setting_id;
        END IF;
    ELSIF (TG_OP = 'DELETE') THEN
        IF (OLD.vote_type = 1) THEN
            UPDATE game_settings SET likes_count = GREATEST(0, likes_count - 1) WHERE id = OLD.game_setting_id;
        ELSE
            UPDATE game_settings SET dislikes_count = GREATEST(0, dislikes_count - 1) WHERE id = OLD.game_setting_id;
        END IF;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger
DROP TRIGGER IF EXISTS tr_update_vote_counts ON game_settings_votes;
CREATE TRIGGER tr_update_vote_counts
AFTER INSERT OR UPDATE OR DELETE ON game_settings_votes
FOR EACH ROW EXECUTE FUNCTION update_game_setting_vote_counts();

-- Inserir categorias padrão
INSERT INTO categories (name) VALUES ('Winlator'), ('Drivers'), ('Ferramentas'), ('DXVK')
ON CONFLICT (name) DO NOTHING;

-- Inserir uma configuração padrão
INSERT INTO app_config (id, dialog_title, dialog_message, show_dialog, latest_version)
VALUES (1, 'Bem-vindo', 'Este é o Winlator Hub oficial!', true, 1)
ON CONFLICT (id) DO NOTHING;

-- SEGURANÇA: Configurar RLS (Row Level Security)
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE repositories ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE game_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE game_settings_votes ENABLE ROW LEVEL SECURITY;

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

    -- Votes
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public to manage own votes' AND tablename = 'game_settings_votes') THEN
        CREATE POLICY "Allow public to manage own votes" ON game_settings_votes FOR ALL USING (true);
    END IF;
END $$;
