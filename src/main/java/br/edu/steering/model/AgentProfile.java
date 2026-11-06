package br.edu.steering.model;

public enum AgentProfile {
    SEEK(
            "01",
            "Seek",
            "BÁSICOS",
            "Buscar sem frear",
            "O pedestre busca o alvo mantendo a velocidade desejada. Ao alcançá-lo, pode"
                    + " ultrapassá-lo: Seek não planeja a parada.",
            "Clique no mapa para mudar o alvo."),
    FLEE(
            "02",
            "Flee",
            "BÁSICOS",
            "Manter uma distância segura",
            "O pedestre foge da ameaça dentro do raio de percepção. Quando está seguro, desacelera."
                    + " O círculo laranja mostra esse raio.",
            "Clique perto do pedestre para reposicionar a ameaça."),
    ARRIVE(
            "03",
            "Arrive",
            "BÁSICOS",
            "Chegar com suavidade",
            "A velocidade desejada diminui na área de chegada. O pedestre freia de forma gradual e"
                    + " para junto ao destino.",
            "Mude o destino e observe a desaceleração."),
    WANDER(
            "04",
            "Wander",
            "BÁSICOS",
            "Explorar sem destino fixo",
            "Um alvo em um círculo à frente muda suavemente de direção. O movimento mantém memória,"
                    + " em vez de sortear um destino distante a cada quadro.",
            "Ative os vetores para acompanhar as mudanças de direção."),
    AVOIDANCE(
            "05",
            "Obstacle Avoidance",
            "NAVEGAÇÃO",
            "Olhar à frente e desviar",
            "Seek fornece a intenção de movimento. O desvio considera a direção atual e a desejada,"
                    + " contornando os obstáculos por tangentes com espaço de conforto.",
            "Use a ferramenta Obstáculo ou reduza o peso do desvio para comparar."),
    PATH(
            "06",
            "Path Following",
            "NAVEGAÇÃO",
            "Encontrar o próximo trecho",
            "O pedestre projeta seu movimento na rota e busca um ponto adiante. Acompanhamento"
                    + " contínuo dos segmentos, com desaceleração no fim.",
            "Use a ferramenta Rota para desenhar um novo percurso."),
    FREE_WALK(
            "07",
            "Passeio livre",
            "COMBINAÇÕES",
            "Wander + desvio de obstáculos",
            "O pedestre combina exploração e antecipação de obstáculos. O desvio tem peso maior"
                    + " para preservar espaço ao redor das árvores e da fonte.",
            "Adicione obstáculos ou compare o desvio com peso zero."),
    SAFE_ROUTE(
            "08",
            "Rota segura",
            "COMBINAÇÕES",
            "Rota + desvio + chegada",
            "Path Following conduz o percurso; Obstacle Avoidance contorna os obstáculos; Arrive"
                    + " assume a aproximação final para uma parada suave.",
            "Compare os pesos e observe o desvio da fonte no meio da rota.");
    private final String number, title, group, subtitle, description, instruction;

    AgentProfile(
            String number,
            String title,
            String group,
            String subtitle,
            String description,
            String instruction) {
        this.number = number;
        this.title = title;
        this.group = group;
        this.subtitle = subtitle;
        this.description = description;
        this.instruction = instruction;
    }

    public String pedestrianName() {
        return switch (this) {
            case SEEK -> "Lia";
            case FLEE -> "Caio";
            case ARRIVE -> "Nina";
            case WANDER -> "Davi";
            case AVOIDANCE -> "Bia";
            case PATH -> "Ivo";
            case FREE_WALK -> "Theo";
            case SAFE_ROUTE -> "Maya";
        };
    }

    public String number() {
        return number;
    }

    public String title() {
        return title;
    }

    public String group() {
        return group;
    }

    public String subtitle() {
        return subtitle;
    }

    public String description() {
        return description;
    }

    public String instruction() {
        return instruction;
    }

    public boolean followsPath() {
        return this == PATH || this == SAFE_ROUTE;
    }

    public boolean avoidsObstacles() {
        return this == AVOIDANCE || this == FREE_WALK || this == SAFE_ROUTE;
    }
}
