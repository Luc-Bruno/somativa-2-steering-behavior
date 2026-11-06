# Análise do enunciado

## Fontes consultadas

- Enunciado_Trabalho_Steering_Behaviors_Prático-1.pdf, duas páginas, fornecido pelo estudante.
- SBCarsEngine.zip, fornecido como template de referência.
- Orientações do estudante na conversa.

O enunciado foi tratado como fonte dos requisitos acadêmicos. As escolhas e autorizações do projeto vêm das orientações do estudante.

## Requisitos acadêmicos

| Item | Exigência | Evidência esperada ao concluir |
| --- | --- | --- |
| R01 | Seek | Agente muda sua velocidade para buscar um alvo |
| R02 | Flee | Agente se afasta de uma ameaça |
| R03 | Arrive | Agente desacelera e chega ao destino |
| R04 | Wander | Agente explora o mapa com movimento contínuo |
| R05 | Obstacle Avoidance | Agente antecipa e evita obstáculos |
| R06 | Path Following | Agente acompanha uma rota |
| R07 | Pequeno mapa de testes | Ambiente visível que permite demonstrar os comportamentos |
| R08 | Pelo menos duas combinações diferentes | Duas configurações demonstráveis, acompanhadas de justificativa |
| R09 | Ajuste dos pesos | Pesos documentados e resultados com equilíbrio de forças e fluidez |
| R10 | Código-fonte em repositório GitHub | Código acessível à professora no momento da entrega |
| R11 | README com explicações | Descrição dos comportamentos efetivamente implementados |
| R12 | Defesa obrigatória | Estudante apresenta e explica o funcionamento |

O enunciado permite aplicar as combinações em um agente por meio de input ou usar dois agentes com combinações diferentes. Não exige um jogo completo, 3D, multiplayer ou uma quantidade adicional de comportamentos.

O trabalho vale 4,0 pontos. Não há uma distribuição de pontos por comportamento no documento recebido. A ausência na defesa implica nota zero.

## Decisões informadas pelo estudante

- Trabalho individual, autorizado pela professora segundo relato do estudante, embora o texto original mencione duplas, trios ou quartetos.
- Linguagem Java.
- Java puro, sem bibliotecas externas. O enunciado não especifica uma versão do Java, IDE ou biblioteca gráfica.
- Interface criada para este projeto.
- Não utilizar o template como base da aplicação.
- Não realizar o trabalho teórico que concede o ponto extra.
- Utilizar personagens humanos em uma praça/parque 2D visto de cima, opção confirmada pelo estudante.
- Entregar até 6 de novembro de 2026, às 18h, no fuso America/Sao_Paulo, conforme prazo informado pelo estudante em 5 de novembro.

O objetivo acadêmico descreve agentes autônomos e não obriga a representação com carros. A troca visual por humanos parece compatível com os requisitos; não foi informado um acordo específico sobre essa representação.

## O que há no template

O ZIP contém sete arquivos Java e não inclui Maven, Gradle ou recursos gráficos externos:

- `Car`, `Settings`, `Vector2`, `Window` e `World` compõem a estrutura de simulação e desenho.
- `Setup` cria os agentes.
- `StudentCar.calculateSteering` retorna um vetor nulo em magnitude; os comportamentos devem ser implementados pelo estudante.

A janela usa Swing e Java2D. O motor já integra força, massa, velocidade e posição ao longo do tempo, limita força e velocidade e desenha vetores de depuração. Os carros atravessam os limites do mapa e reaparecem do lado oposto.

Não há implementação dos seis comportamentos exigidos, obstáculos ou rotas no template inspecionado. O código importa `RandomGenerator` e utiliza `Math.clamp`, portanto não é adequado assumir compatibilidade direta com um ambiente Java antigo.

Nenhum arquivo do template foi incorporado ao projeto.

## Direção técnica e propostas

- Ambiente 2D visto de cima, com pedestres em um parque ou praça.
- Interface própria com Swing e Java2D, disponíveis na plataforma Java.
- Demonstração individual de cada comportamento.
- Combinação A: Wander + Obstacle Avoidance, para passeio sem destino fixo.
- Combinação B: Path Following + Obstacle Avoidance + Arrive, para percurso com parada no destino final.
- Controles para pesos, pausa, reinício e vetores, caso sejam úteis para a apresentação.
- Separação entre matemática, comportamentos, simulação e interface para facilitar explicação e manutenção.

As propostas desta seção não constituem funcionalidades já implementadas.

## Pontos ainda abertos

1. Data da arguição.
2. Detalhes do estilo visual, que podem ser definidos durante a implementação.
3. Entrega em PDF: o cabeçalho menciona PDF, enquanto a seção de entregáveis especifica GitHub com código e README. Não há instrução suficiente para determinar o conteúdo desse PDF.
4. Forma de dar acesso à professora caso o repositório permaneça privado.

Esses pontos não impedem o início da implementação.
