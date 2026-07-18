# DailyEnglish

Um live wallpaper (papel de parede animado) para Android que mostra uma palavra nova de inglês intermediário (B1/B2) direto na tela do celular — sem precisar abrir o app.

## Como funciona

- A cada **12 horas** (à meia-noite e ao meio-dia, no horário local do aparelho) uma nova palavra aparece automaticamente no papel de parede.
- O banco de dados (`app/src/main/assets/words.json`) tem **730 palavras** — uma para cada meia-diária do ano, sem repetição dentro do mesmo ciclo anual.
- Cada entrada traz: palavra em inglês, tradução em português, frase de exemplo em inglês e a tradução da frase.
- Tudo funciona **offline**: não há servidor, API externa ou necessidade de o app rodar em segundo plano. O Android é quem desenha o papel de parede sozinho, seguindo o relógio do sistema.

## Passo único obrigatório

Por segurança, o Android não permite que nenhum app se defina como papel de parede sozinho — é preciso um toque do usuário uma única vez. Ao abrir o app, toque em **"Definir como papel de parede"**; isso abre o seletor do sistema. Depois disso, o app não precisa mais ser aberto: o Android atualiza a palavra sozinho a cada 12 horas.

## Estrutura do projeto

```
app/src/main/java/com/larissamilena/dailyenglish/
├── MainActivity.kt                     # Tela única com botão para definir o wallpaper
├── data/
│   ├── Word.kt                         # Modelo de dados de uma palavra
│   └── WordRepository.kt               # Carrega words.json e escolhe a palavra atual
└── wallpaper/
    └── DailyWordWallpaperService.kt    # Live wallpaper (Canvas) que desenha a palavra na tela

app/src/main/assets/words.json          # Banco com 730 palavras (inglês, tradução, exemplo)
app/src/main/res/xml/live_wallpaper.xml # Descritor exigido pelo Android para live wallpapers
```

## Rotação das palavras

O índice da palavra do momento é calculado por `dia_do_ano * 2 + (0 se for manhã, 1 se for tarde/noite)`, sem precisar salvar estado nem rodar tarefas em segundo plano. Isso garante que a mesma sequência de 730 palavras se repete todo ano.

## Build

Este projeto foi criado com Kotlin + Android Gradle Plugin, usando Views (XML) tradicionais — sem Jetpack Compose, já que o motor de live wallpaper do Android exige `Canvas`/`WallpaperService`, não suporta Compose diretamente.

Para compilar, abra a pasta no Android Studio (ele baixa o Gradle Wrapper e o Android SDK automaticamente) ou rode:

```
./gradlew assembleDebug
```

> Este projeto foi montado num ambiente sem Android SDK instalado, então o build completo (compilação/APK) não foi validado aqui — apenas a estrutura de arquivos, XML e sintaxe Kotlin. Recomenda-se abrir no Android Studio e rodar um build local antes da primeira instalação em um aparelho.

## Personalização

- **Trocar as palavras**: edite `app/src/main/assets/words.json` (mantenha os 4 campos: `word`, `translation`, `example`, `exampleTranslation`).
- **Mudar as cores do fundo**: `app/src/main/res/values/colors.xml`.
- **Mudar a frequência de troca**: ajuste a lógica em `WordRepository.getCurrentWord()`.
