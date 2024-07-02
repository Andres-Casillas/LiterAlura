package com.alura.literalura.principal;

import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;

import java.util.Optional;
import java.util.Scanner;
import java.util.List;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/";
    private final String URL_SEARCH = "?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private final LibroRepository repositoryLibro;
    private final AutorRepository repositoryAutor;

    public Principal(LibroRepository repositoryLibro, AutorRepository repositoryAutor) {
        this.repositoryLibro = repositoryLibro;
        this.repositoryAutor = repositoryAutor;
    }

    public void mostrarMenu(){
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                -------------------- LITERALURA --------------------
                
                    1 - Buscar libro por titulo
                    2 - Lista de todos los libros
                    3 - Listar autores
                    4 - Listar autores vivos en determinado año
                    5 - Lista de libros por idiomas
                
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    System.out.println("-------------------- Buscar Libro --------------------\n");
                    busquedaLibro();
                    pause();
                    break;
                case 2:
                    System.out.println("-------------------- Listar Libros --------------------\n");
                    mostrarTodo();
                    pause();
                    break;
                case 3:
                    System.out.println("-------------------- Listar Autores --------------------\n");
                    mostrarAutores();
                    pause();
                    break;
                case 4:
                    System.out.println("-------------------- Listar Autores --------------------\n");
                    mostrarAutoresVivos();
                    pause();
                    break;
                case 5:
                    System.out.println("-------------------- Listar por Idioma --------------------\n");
                    mostrarLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    private DatosBusqueda getDatosLibro(){
        System.out.println("Escribe el nombre del libro que deseas buscar:");
        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + URL_SEARCH + nombreLibro.replace(" ", "%20"));

        return conversor.obtenerDatos(json,DatosBusqueda.class);
    }

    private void busquedaLibro(){
        DatosBusqueda datosBusqueda = getDatosLibro();
        if (datosBusqueda == null || datosBusqueda.resultado().isEmpty()) {
            System.out.println("Libro no encontrado");
        }


        DatosLibro primerLibro = datosBusqueda.resultado().get(0);
        Libro libro = new Libro(primerLibro);

        Optional<Libro> libroExistenteOptional = repositoryLibro.findByTitulo(libro.getTitulo());
        if (libroExistenteOptional.isPresent()) {
            System.out.println("\nEl libro ya está registrado\n");
        }
        if (primerLibro.autor().isEmpty()) {
            System.out.println("Sin autor");
            return;
        }

        DatosAutor datosAutor = primerLibro.autor().get(0);
        Autor autor = new Autor(datosAutor);
        Optional<Autor> autorOptional = repositoryAutor.findByNombre(autor.getNombre());

        Autor autorExistente = autorOptional.orElseGet(() -> repositoryAutor.save(autor));
        libro.setAutor(autorExistente);
        repositoryLibro.save(libro);

        System.out.printf("""
                ---------- Libro ----------
                Título: %s
                Autor: %s
                Idioma: %s
                Descargas: %d
                ---------------------------
                """, libro.getTitulo(), autor.getNombre(), libro.getLenguaje(), libro.getDescargas());
    }

    private DatosBusqueda getTodo(String url){
        var json = consumoApi.obtenerDatos(url);

        return conversor.obtenerDatos(json,DatosBusqueda.class);
    }

    private void mostrarTodo() {
        var opcion = -1;
        DatosBusqueda datosBusqueda = getTodo("https://gutendex.com/books/");
        if (datosBusqueda == null || datosBusqueda.resultado().isEmpty()) {
            System.out.println("Error en la busqueda, intente de nuevo.");
            return;
        }

        System.out.println("Total de libros en la web: " + datosBusqueda.encontrados());
        System.out.println("\n");
        System.out.println("Mostrar libros    1 - De la web.");
        System.out.println("                  2 - Buscados anteriormente.");
        System.out.println("                  3 - Regresar al menu.");
        opcion = teclado.nextInt();
        if(opcion == 1){
            int i = 0;
            while(true){
                DatosLibro libro = datosBusqueda.resultado().get(i);
                System.out.println(i + ": " + libro.titulo() + ", " + libro.autor().get(0).nombre() + ", " + libro.idiomas() + ", " + libro.descargas());

                if(i == datosBusqueda.resultado().size()-1){
                    System.out.println("\n");
                    System.out.println("Mostrar siguiente pagina    1 - Mostrar.");
                    System.out.println("                            2 - Regresar al menu.");
                    opcion = teclado.nextInt();
                    if(opcion != 2){
                        datosBusqueda = getTodo(datosBusqueda.siguiente());
                        i = 0;
                    }else {
                        break;
                    }
                }
                i++;
            }
        } else if (opcion == 2) {
            List<Libro> libros = repositoryLibro.findAll();

            if (libros.isEmpty()) {
                System.out.println("No se encontraron libros registrados.");
                return;
            }

            System.out.println("----- Libros Registrados -----");
            libros.forEach(System.out::println);
            System.out.println("-------------------------------");
        }
    }

    private void mostrarAutores(){
        List<Autor> autores = repositoryAutor.findAll();

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores registrados.");
            return;
        }

        System.out.println("----- Autores Registrados -----");
        autores.forEach(System.out::println);
        System.out.println("-------------------------------");
    }

    private void mostrarAutoresVivos(){
        System.out.println("Introduce el año para listar los autores vivos:");
        while (!teclado.hasNextInt()) {
            System.out.println("Formato inválido, ingrese un número válido para el año");
            teclado.nextLine();
        }
        int anio = teclado.nextInt();
        teclado.nextLine();

        List<Autor> autores = repositoryAutor.findAutoresVivosEnAnio(anio);

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + anio);
        } else {
            System.out.println("----- Autores Vivos en el Año " + anio + " -----");
            autores.forEach(System.out::println);
            System.out.println("---------------------------------------------");
        }
    }

    private void mostrarLibrosPorIdioma() {
        System.out.println("Selecciona el lenguaje/idioma que deseas buscar: ");
        while (true) {
            String opciones = """
                    1. en - Inglés
                    2. es - Español
                    3. fr - Francés
                    4. pt - Portugués
                    0. Volver a las opciones anteriores
                    """;
            System.out.println(opciones);
            while (!teclado.hasNextInt()) {
                System.out.println("Formato inválido, ingrese un número que esté disponible en el menú");
                teclado.nextLine();
            }
            int opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1 -> mostrarIdioma(Idioma.en);
                case 2 -> mostrarIdioma(Idioma.es);
                case 3 -> mostrarIdioma(Idioma.fr);
                case 4 -> mostrarIdioma(Idioma.pt);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Opción inválida");
            }
        }
    }

    private void mostrarIdioma(Idioma idioma) {
        List<Libro> librosPorIdioma = repositoryLibro.findByLenguaje(idioma);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("No se encontraron libros en " + idioma.getIdiomaEspanol());
        } else {
            System.out.printf("----- Libros en %s ----- %n", idioma.getIdiomaEspanol());
            librosPorIdioma.forEach(System.out::println);
            System.out.println("-----------------------------");
        }
        pause();
    }

    private void pause() {
        System.out.println("Presiona Enter para continuar...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
