# Somativa 2 - Steering Behavior

Projeto acadêmico em Java para demonstrar Steering Behaviors com agentes humanos em um ambiente 2D e interface própria.

**Estado:** levantamento de requisitos. A aplicação ainda não foi implementada.

## Escopo confirmado

- Desenvolvimento individual, conforme acordo com a professora informado pelo estudante.
- Implementação em Java.
- Java puro, com Swing e Java2D, sem bibliotecas externas.
- Organização MVC: Model, View e Controller com responsabilidades separadas.
- Interface própria, sem utilizar o projeto SBCarsEngine como base.
- Personagens humanos em uma praça/parque 2D visto de cima, conforme preferência confirmada pelo estudante.
- Trabalho prático de 4,0 pontos; o trabalho teórico de bônus está fora do escopo.
- Entrega prática: código e README em repositório público, com testes demonstrados pela interface gráfica.
- Prazo informado: **6 de novembro de 2026, às 18h (America/Sao_Paulo)**.

Repositório público: https://github.com/Luc-Bruno/somativa-2-steering-behavior.

## Requisitos do trabalho prático

| Requisito | Estado |
| --- | --- |
| Seek: mover-se em direção a um alvo | A implementar |
| Flee: afastar-se de uma ameaça | A implementar |
| Arrive: aproximar-se de um destino com desaceleração | A implementar |
| Wander: explorar o ambiente com mudanças suaves de direção | A implementar |
| Obstacle Avoidance: desviar de obstáculos | A implementar |
| Path Following: acompanhar um caminho | A implementar |
| Pequeno mapa de testes | A implementar |
| Pelo menos duas combinações distintas de comportamentos | A implementar |
| Ajuste de pesos, equilíbrio de forças e fluidez | A validar |
| Explicação dos comportamentos e das combinações neste README | A completar após implementação |

## Direção da implementação

Um parque ou praça visto de cima, com pedestres, obstáculos e caminhos. A interface será construída com Swing e Java2D. Pode permitir selecionar comportamentos, definir alvos, ajustar pesos e visualizar vetores para apoiar a demonstração e a defesa.

Duas combinações candidatas:

1. **Wander + Obstacle Avoidance:** um pedestre passeia e desvia dos obstáculos.
2. **Path Following + Obstacle Avoidance + Arrive:** um pedestre percorre uma rota, evita obstáculos e desacelera no destino final.

As combinações e os controles são propostas de implementação; os parâmetros serão ajustados na simulação.

## Entrega e defesa

O trabalho será entregue como projeto prático: repositório GitHub público com código-fonte e README explicando os comportamentos implementados. A arguição é obrigatória e sua ausência resulta em nota zero.

Os testes serão executados e demonstrados à professora pela própria interface gráfica, com cenários para os seis comportamentos e para as combinações. O estudante esclareceu que a menção ao PDF se refere à alternativa teórica, que não será adotada neste projeto.

## Pendências

- Data da defesa.
- Detalhes visuais e parâmetros da simulação.
- Instruções de execução, que serão incluídas quando a aplicação existir.

O detalhamento da análise está em [docs/requisitos.md](docs/requisitos.md), e a organização MVC está em [docs/arquitetura.md](docs/arquitetura.md).
