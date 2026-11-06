# Roteiro de demonstração

Abra `Executar.cmd`. A aplicação começa em **Rota segura**, com a combinação dos três comportamentos. O botão **Como demonstrar** mostra um resumo dentro da interface.

## Apresentação dos comportamentos

| Etapa | Ação na interface | O que mostrar e explicar |
| --- | --- | --- |
| 1 — Seek | Selecione 1, clique em um alvo livre e ative Vetores | A velocidade desejada aponta para o alvo; o agente corrige sua velocidade e pode ultrapassá-lo porque não planeja frear |
| 2 — Flee | Selecione 2 e clique perto do agente para colocar a ameaça | O agente se afasta enquanto a ameaça está no raio de percepção e freia quando está seguro |
| 3 — Arrive | Selecione 3 e clique em outro destino | O círculo indica a área de desaceleração; a velocidade cai perto do alvo e o agente para |
| 4 — Wander | Selecione 4 e observe o círculo de depuração à frente | Um alvo local muda suavemente de direção, mantendo memória e produzindo exploração |
| 5 — Obstacle Avoidance | Selecione 5; acompanhe o sensor e a passagem pela fonte | O sensor detecta a fonte antes do contato; o agente busca uma direção tangente e mantém espaço de conforto |
| 5b — Comparação | Reinicie o cenário 5 e zere o peso Obstacle Avoidance antes de ele chegar à fonte | Seek continua buscando o alvo, mas o agente toca na fonte; o contador registra a salvaguarda de colisão |
| 6 — Path Following | Selecione 6; observe a projeção e o alvo adiante na rota | O agente acompanha segmentos e desacelera no fim; os pontos são fornecidos, não calculados por A* |

## Apresentação das combinações

### Passeio livre — 7

Mostre a exploração e o desvio atuando juntos. Os pesos iniciais são Wander **0,9** e Obstacle Avoidance **2,6**. Explique que o desvio precisa superar a intenção de explorar quando há risco à frente. Adicione um canteiro em uma área livre e observe a reação. O mapa foi simplificado para sete obstáculos; o contorno externo exibido com Vetores representa a margem de segurança.

### Rota segura — 8

Mostre que a rota passa pela fonte, mas o agente a contorna e retorna ao percurso. Path Following tem peso **1,0**, Obstacle Avoidance **2,8** e Arrive **0,9**. A chegada só passa a contribuir no trecho final para não encurtar o trajeto desde o início.

Para mostrar interação, selecione **Rota**, desenhe no mínimo dois pontos livres e pressione **Continuar**. A rota pode cruzar um obstáculo no meio: o agente tentará contorná-lo. Reiniciar restaura a configuração validada do mapa.

## Recursos úteis durante a arguição

- **Espaço:** congele a simulação para explicar os vetores.
- **N / 1 passo:** avance um quadro para mostrar a integração.
- **0,5×:** observe uma curva ou desaceleração lentamente.
- **Sliders:** relacione peso, força máxima e velocidade com a trajetória.
- **R / Reiniciar:** restaure o cenário e a semente de Wander para comparar ajustes.
- **Contatos físicos:** diferencie desvio antecipado de correção após colisão.

## Explicação do código

1. `App` conecta as partes MVC.
2. `SimulationController` recebe input e coordena atualizações.
3. `World` mantém o mapa e o agente do cenário.
4. Os seis comportamentos retornam forças vetoriais.
5. `SteeringMixer` soma forças multiplicadas por seus pesos.
6. `Agent.integrate` limita força/velocidade e atualiza posição com `dt`.
7. `MainView` e `MapPanel` apresentam o estado; não calculam steering.

A opção de alternar as combinações por input em um único agente está expressamente prevista na página 1, seção 2, do enunciado. Todos os comportamentos solicitados continuam implementados e demonstráveis.
