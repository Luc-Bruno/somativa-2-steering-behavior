# Organização MVC

A aplicação será organizada em Model, View e Controller, conforme solicitado pelo estudante. Esta é a estrutura planejada; a implementação da aplicação ainda não começou.

## Model

Responsável pelo estado e pelas regras da simulação:

- Vetores, forças, aceleração, velocidade e posição.
- Agentes humanos, obstáculos, alvos e caminhos.
- Seek, Flee, Arrive, Wander, Obstacle Avoidance e Path Following.
- Combinações de comportamentos e seus pesos.
- Evolução do mundo a partir de um intervalo de tempo.
- Configurações dos cenários de demonstração.

O Model não dependerá de Swing, de Java2D ou de componentes visuais. Seus dados representarão a simulação, sem executar desenho ou tratar cliques.

## View

Responsável pela apresentação com Swing e Java2D:

- Janela e organização dos painéis.
- Desenho da praça/parque, dos pedestres, dos obstáculos e das rotas.
- Desenho opcional de vetores e outras informações da simulação.
- Controles para selecionar cenários e ajustar parâmetros.
- Exibição do estado de pausa e das informações do agente selecionado.

A View consultará o estado do Model para desenhar. As ações do usuário serão encaminhadas ao Controller, sem implementar fórmulas de steering nos componentes gráficos.

## Controller

Responsável por coordenar a interação e a execução:

- Receber cliques, comandos de teclado e alterações nos controles.
- Traduzir as entradas em alterações de alvos, cenários e parâmetros do Model.
- Coordenar pausa, reinício e avanço da simulação.
- Medir o tempo decorrido, solicitar a atualização do Model e pedir o redesenho da View.

As atualizações do Model e a leitura feita pela View serão coordenadas na thread de eventos do Swing, mantendo uma sequência consistente de atualização e desenho.

## Estrutura prevista

```text
src/main/java/br/edu/steering/
  App.java
  model/
    math/
    behavior/
    scenario/
  view/
  controller/
```

`App` será apenas o ponto de entrada que cria e conecta as partes. Os pacotes internos do Model separarão matemática, comportamentos e cenários. Agentes, mundo, obstáculos e rotas também pertencerão ao Model.

## Fluxo de interação

1. O usuário seleciona um cenário ou interage com o mapa na View.
2. O Controller interpreta a ação e configura o Model.
3. A cada atualização, o Model calcula as forças e evolui os agentes.
4. O Controller solicita que a View desenhe o estado atualizado.

Os testes apresentados à professora serão cenários acessíveis pela interface, cobrindo os seis comportamentos e pelo menos duas combinações. A mesma separação MVC será preservada nesses cenários.
