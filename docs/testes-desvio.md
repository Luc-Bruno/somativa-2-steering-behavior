# Regressões de desvio de obstáculos

A versão inicial foi submetida a dez casos prolongados após o relato de travamento. Quatro falharam: passeio livre de dez minutos, rota segura, aproximação da fonte de norte para sul e rota vertical.

## Comparação reproduzível

| Medida | Antes | Depois |
| --- | --- | --- |
| Obstáculos fixos no mapa | 14 | 7 |
| Margem de segurança configurada | 18 unidades | 36 unidades + faixa suave de conforto |
| Contatos no passeio de dez minutos | 329 | 0 |
| Maior intervalo de baixa velocidade nesse passeio | 19,05 s | 0,07 s |
| Chegada na rota segura | 28,33 s | 14,20 s |
| Rota vertical | Não chegou em 80 s; ficou lenta por 75,80 s | Chegou em 6,10 s e parou no destino |
| Folga mínima no desvio padrão | 18,00 unidades | 46,25 unidades |

Os dez casos comparativos passaram após a correção. Dois testes adicionais de passeio, com atualização a 30 e 144 Hz, também passaram. A menor folga nos doze casos ficou acima de 40 unidades, com zero contatos físicos.

## O que os testes exigem

- Chegar ao alvo nos casos com destino; nas rotas, terminar a execução a menos de oito unidades do destino e com velocidade inferior a uma unidade/s.
- Não registrar contatos físicos.
- Manter folga mínima de 32 unidades entre o corpo do pedestre e os obstáculos.
- Não permanecer por quatro segundos na mesma vizinhança de raio 20 enquanto ainda precisa avançar.
- Não ficar por três segundos consecutivos abaixo de oito unidades/s, fora da região de chegada.

O diagnóstico inicial avaliou contatos e progresso; a exigência de 32 unidades de folga foi adicionada à regressão para validar o afastamento solicitado. Esses critérios distinguem travamento de uma parada normal no destino. Wander não tem destino final; o campo chegada=-1 no resumo indica que a chegada não se aplica aos testes de passeio.

## Como reproduzir

Execute `Verificar.cmd`. O comando roda primeiro as 27 verificações gerais, depois `br.edu.steering.model.AvoidanceStressTest`. O resumo e as trajetórias amostradas ficam em `output/stress/`.

As doze execuções somam **1810 segundos simulados (30 min e 10 s)**; o teste é numérico e roda mais rápido que o tempo real. Inclui quatro direções de aproximação, uma passagem com obstáculo adicionado, força reduzida, velocidade aumentada e diferentes cadências de atualização.

O [resumo antes da correção](diagnostico-desvio-antes.txt) e o [resumo depois da correção](testes-desvio.txt) estão versionados. O resultado cobre esses cenários e os parâmetros descritos. Pesos extremos e passagens artificialmente fechadas continuam sendo experimentos de comportamento local, sem planejamento global de caminhos.
