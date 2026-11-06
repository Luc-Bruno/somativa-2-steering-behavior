# Arquitetura MVC

A aplicação usa Java 17+, Swing e Java2D, sem dependências externas. Cada cenário é configurado no mesmo mapa de testes e pode ser selecionado por input na interface, como permitido pelo enunciado.

## Model

- `Vec2`: vetores imutáveis e operações geométricas.
- `Agent`: posição, velocidade, orientação, pesos, limites, semente aleatória, progresso na rota e estado de depuração.
- `AgentProfile`: nomes e descrições dos seis comportamentos e das duas combinações.
- `World`: criação do mapa e do agente de cada cenário, atualização, edição de alvos/rotas/obstáculos e salvaguarda física de colisão.
- `Route`: projeção em segmentos e distância acumulada.
- `Obstacle`: volume circular e tipo de elemento do parque.
- `SteeringBehavior`: contrato para produzir uma força vetorial.
- `Seek`, `Flee`, `Arrive`, `Wander`, `ObstacleAvoidance` e `PathFollowing`: implementação de cada comportamento.
- `SteeringMixer`: soma ponderada e contenção auxiliar das bordas.

O Model não importa `java.awt` nem `javax.swing`. Regras de movimentação não dependem de componentes ou tamanho da janela. O mundo tem 1080×720 unidades e a View aplica escala para caber na área disponível.

## View

- `MainView`: seleção de cenários, botões, sliders, informações do agente e instruções.
- `MapPanel`: desenho da praça, personagens, obstáculos, rotas, alvos, rastros e vetores. Converte coordenadas do clique para unidades do mundo.
- `Theme` e `PillButton`: tipografia, cores e componentes visuais.

A View consulta o estado do Model para desenhar, mas não altera posição, pesos ou alvos diretamente. O contrato `MainView.Actions` encaminha comandos ao Controller.

## Controller

`SimulationController` implementa as ações da View. Interpreta eventos, altera parâmetros do agente, carrega os cenários, controla pausa, reinício e edição de rotas, e coordena o ciclo da simulação.

Um `javax.swing.Timer` solicita atualizações aproximadamente a cada 16 ms. O tempo real é medido com `System.nanoTime`; atrasos grandes são limitados e o Model divide o intervalo em subpassos de até 1/120 s. A View solicita o redesenho ao final da atualização.

Eventos, alterações no Model e pintura são serializados na **Event Dispatch Thread** do Swing. O Model permanece independente desse mecanismo: pode ser simulado por chamadas diretas a `World.update(dt)`, como nas verificações numéricas.

## Entrada

`App` cria e conecta Model, View e Controller, configura a janela e inicia o ciclo. Ao fechar a janela, o Timer é encerrado.

## Fluxo de uma interação

1. O usuário muda um peso na View.
2. O evento chama `SimulationController.changeWeight`.
3. O Controller atualiza o peso no Agent.
4. Na próxima atualização, o Mixer aplica esse peso à força do comportamento.
5. O Model integra a força; a View desenha o estado atualizado.

A edição de caminhos pausa a simulação e exige dois pontos válidos antes de continuar. O reinício recria o mapa e restaura os parâmetros e a semente aleatória do cenário.
