-- Comando para criar a tabela de repositórios
CREATE TABLE repositories (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    owner TEXT NOT NULL,
    repo TEXT NOT NULL,
    description TEXT
);

-- Comando para criar a tabela de configuração do app (diálogo inicial)
CREATE TABLE app_config (
    id INT PRIMARY KEY DEFAULT 1,
    dialog_title TEXT,
    dialog_message TEXT,
    show_dialog BOOLEAN DEFAULT FALSE
);

-- Inserir uma configuração padrão
INSERT INTO app_config (id, dialog_title, dialog_message, show_dialog)
VALUES (1, 'Bem-vindo', 'Este é o Winlator Hub oficial!', true)
ON CONFLICT (id) DO NOTHING;

-- Inserir os repositórios iniciais
INSERT INTO repositories (name, owner, repo, description) VALUES
('Winlator Oficial', 'brunodev85', 'winlator', 'Versão oficial do Winlator'),
('Winlator Brasil', 'winlatorbrasil', 'Winlator-Brasil', 'Versão otimizada pela comunidade brasileira'),
('Winlator Afei', 'afeimod', 'winlator-mod', 'Mod do Winlator por Afei'),
('Winlator Frost', 'MrPhryaNikFrosty', 'Winlator-Frost', 'Mod do Winlator por Frost'),
('Winlator Ajay', 'ajay9634', 'winlator-ajay', 'Mod do Winlator por Ajay'),
('WINLATOR LUDASHI', 'Succubussix', 'winlator-bionic-glibc', 'Versão Bionic Glibc'),
('WinlatorOSS', 'Mart-01-oss', 'WinlatorOSS', 'Winlator Open Source Software'),
('Drivers Turnip', 'K11MCH1', 'WinlatorTurnipDrivers', 'Drivers Turnip para Winlator');
