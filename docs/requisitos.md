# Requisitos e decisões do projeto

## Fontes

- Enunciado_Trabalho_Steering_Behaviors_Prático-1.pdf, páginas 1 e 2, fornecido pelo estudante.
- SBCarsEngine.zip, inspecionado como referência de estrutura e sem código incorporado ao projeto.
- Orientações e esclarecimentos do estudante na conversa.

O documento acadêmico é a fonte dos requisitos; as autorizações e escolhas do projeto vêm do estudante.

## Correspondência com a entrega

| Requisito | Onde demonstrar |
| --- | --- |
| Seek | Cenário 1 |
| Flee | Cenário 2 |
| Arrive | Cenário 3 |
| Wander | Cenário 4 |
| Obstacle Avoidance | Cenário 5 |
| Path Following | Cenário 6 |
| Mapa de testes | Praça com obstáculos visíveis e editáveis |
| Primeira combinação | Cenário 7: Wander + Obstacle Avoidance |
| Segunda combinação | Cenário 8: Path Following + Obstacle Avoidance + Arrive |
| Pesos, equilíbrio de forças e fluidez | Sliders e vetores; justificativas no README |
| GitHub com código e README | Repositório público Luc-Bruno/somativa-2-steering-behavior |
| Defesa obrigatória | Guia dentro da aplicação e roteiro de demonstração |

## Forma de apresentar as combinações

A página 1, seção 2, do enunciado afirma:

> As combinações podem ser feitas a partir de input em um único agente, ou implementar dois agentes com combinações distintas. Fica a critério da equipe.

O projeto adota **seleção por input na interface**. São oito cenários reproduzíveis, com um agente em cada configuração. Não é necessário manter todos os comportamentos ativos simultaneamente para usar essa opção. Os seis comportamentos e as duas combinações estão disponíveis para demonstração.

## Decisões confirmadas pelo estudante

- Desenvolvimento individual, autorizado pela professora segundo seu relato.
- Java puro, sem bibliotecas externas; o enunciado não exige uma versão específica, IDE ou biblioteca gráfica.
- Organização MVC.
- Interface própria, sem usar o template como base.
- Personagens humanos em uma praça/parque 2D visto de cima.
- Repositório público.
- Trabalho prático em código, com testes apresentados pela interface gráfica.
- Sem trabalho teórico ou ponto extra. O estudante esclareceu que a menção ao PDF corresponde à alternativa de entrega teórica, que não será adotada.
- Prazo informado em 5 de novembro: 6 de novembro de 2026, às 18h, America/Sao_Paulo.

O trabalho prático vale 4,0 pontos. O documento não distribui pontos por comportamento. A ausência na arguição implica nota zero.

## Escopo técnico

O programa usa Java 17+ com Swing e Java2D. Os elementos físicos são círculos; personagens usam um modelo simples de aceleração, velocidade e orientação. Caminhos são definidos por pontos e acompanhados por projeção nos segmentos. As forças são combinadas por soma ponderada, com limites e pesos ajustáveis.

Os cenários permitem experimentar efeitos de pesos inadequados, inclusive zerando o desvio e observando contatos físicos. Não há implementação do trabalho teórico de bônus.
