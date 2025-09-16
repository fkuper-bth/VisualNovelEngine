# About

This repository implements a [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html)
library which can be used to create visual novels and was generated using the [multiplatform-library-template](https://github.com/Kotlin/multiplatform-library-template)
published by JetBrains. 

It depends on the [StoryEngine](https://github.com/fkuper-bth/StoryEngine)
library to handle the story imports and logic and currently targets the platforms iOS, Android and 
Web via WebAssembly.

For examples on how to use this library you can refer to the [ExampleConsumerApplication.kt](library/src/commonMain/kotlin/etc/utils/preview/ExampleConsumerApplication.kt)
found inside this repository or take a look at a bigger example application which uses the library 
[VisualNovelExample](https://github.com/fkuper-bth/VisualNovelExample).

## Why was this repository created?

This repository was created as part of a larger project which was created for a thesis. All the relevant projects
can be found [here](https://github.com/fkuper-bth).

A more detailed explanation of this repository and how it relates to the other projects can also be
found in the [thesis](https://github.com/fkuper-bth/Thesis) though it is written in German.

## Publishing

Currently this project is only set up to publish in a local maven repository 
(see [library/build.gradle.kts](library/build.gradle.kts) for more details).