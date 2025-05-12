# Aplicativo Calculadora

## Visão Geral do Projeto

O Aplicativo Calculadora Sistemas Tech é uma calculadora Android desenvolvida em Kotlin. Ele oferece operações aritméticas básicas, funções científicas e mantém um registro de memória para cálculos. O design prioriza fácil extensibilidade e tratamento de erros robusto.

## Funcionalidades

Operações Básicas: Adição (+), Subtração (-), Multiplicação (×), Divisão (÷)

Funções Científicas: Potência, Raiz Quadrada, Logaritmo

Memória: Armazenamento e recuperação de um único valor de memória (MC, MR, M+, M-)

Validação de Entrada: Prevenção de entradas inválidas (operadores duplicados, parênteses desequilibrados)

Tratamento de Erros: Exibe "Erro" em expressões inválidas em vez de travar a aplicação

## Iniciando

### Pré-requisitos

Android Studio

SDK Android API

Kotlin 1.8+

### Instalação

Clone o repositório:

git clone [https://github.com/seu-usuario/sistemas-tech-calculator.git](https://github.com/PedroLucasCG/calculadora/)

Abra o projeto no Android Studio.

Aguarde o Gradle sincronizar e baixar dependências.

Execute o app em um emulador ou dispositivo físico.

## Como Usar

Toque em dígitos e operadores para montar uma expressão.

Pressione = para calcular.

Use C para limpar.

Use MR para lembrar memória, M+ para adicionar à memória, M- para subtrair da memória, MC para limpar memória.

## Tratamento de Erros

Expressões vazias ou incompletas exibem Erro em vez de travar.

Parênteses desequilibrados e sequências inválidas são bloqueados antes da avaliação.
