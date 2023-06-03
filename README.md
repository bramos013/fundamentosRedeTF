# Trabalho Final de Fundamentos de Redes de Computadores

### Integrantes: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer

### Semestre: 2023/1

## Como executar a aplicação?

Para executar a aplicação, execute os comandos abaixo, na pasta em que foi baixado o projeto:

```sh
javac -d out/ -sourcepath src/ ./src/roteador/Router.java
cd out && jar cfm trabalho-final.jar ../META-INF/MANIFEST.MF .
java -jar trabalho-final.jar <endereco IP do host>
```

Além disso, na mesma pasta em que se roda o arquivo .jar, é preciso ter um arquivo de nome IPVizinhos.txt, onde cada linha representa um endereço IP de um vizinho, conforme exemplo abaixo:

127.0.0.1

192.168.1.1

192.168.1.2

192.168.1.3

192.168.1.4
