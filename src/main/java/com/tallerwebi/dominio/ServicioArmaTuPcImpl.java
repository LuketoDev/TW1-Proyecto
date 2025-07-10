package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
import com.tallerwebi.presentacion.ProductoCarritoDto;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

// agregar transaccional
// agregar service
@Service("servicioArmaTuPc")
@Transactional
public class ServicioArmaTuPcImpl implements ServicioArmaTuPc {

    private RepositorioComponente repositorioComponente;
    private ServicioPrecios servicioPrecios;
    private ServicioCompatibilidades servicioCompatibilidades;

    private final Map<String, String> correspondenciaDeVistaConTablasEnLaBD = new LinkedHashMap<>() {{
        put("procesador", "Procesador");
        put("motherboard", "Motherboard");
        put("cooler", "CoolerCPU");
        put("memoria", "MemoriaRAM");
        put("gpu", "PlacaDeVideo");
        put("almacenamiento", "Almacenamiento");
        put("fuente", "FuenteDeAlimentacion");
        put("gabinete", "Gabinete");
        put("monitor", "Monitor");
        put("periferico", "Periferico");
    }};



    @Autowired
    public ServicioArmaTuPcImpl(RepositorioComponente repositorioComponente, ServicioPrecios servicioPrecios , ServicioCompatibilidades servicioCompatibilidades) {
        this.repositorioComponente = repositorioComponente;
        this.servicioPrecios = servicioPrecios;
        this.servicioCompatibilidades = servicioCompatibilidades;
    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {
        return this.repositorioComponente.obtenerComponentePorId(idComponente);
    }

    @Override
    public ComponenteDto obtenerComponenteDtoPorId(Long idComponente) {
        return new ComponenteDto(this.obtenerComponentePorId(idComponente));
    }

    @Override
    public List<ProductoCarritoArmadoDto> pasajeAProductoArmadoDtoParaAgregarAlCarrito(ArmadoPcDto armadoPcDto) {

        List<ProductoCarritoArmadoDto> productoCarritoDtos = new ArrayList<>();

        Map<Long, Integer> idDeComponentesDelArmadoYCantidades = armadoPcDto.getIdYCantidadComponentes();

        for(Map.Entry<Long, Integer> componente : idDeComponentesDelArmadoYCantidades.entrySet()){

            Componente componenteEntidad = this.repositorioComponente.obtenerComponentePorId(componente.getKey());
            ProductoCarritoArmadoDto productoCarritoDto = new ProductoCarritoArmadoDto(componenteEntidad , componente.getValue());
            productoCarritoDtos.add(productoCarritoDto);
        }

        return productoCarritoDtos;
    }

    @Override
    public Integer obtenerWattsTotalesDeArmado(ArmadoPcDto armadoPcDto) {
        ArmadoPc armadoPc = this.servicioCompatibilidades.completarEntidadArmadoPcParaEvaluarFuente(armadoPcDto.obtenerEntidad());
        return this.servicioCompatibilidades.obtenerWattsDeArmado(armadoPc);
    }



    @Override
    public List<ComponenteDto> obtenerListaDeComponentesCompatiblesDto(String tipoComponente, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException {

        String tablaDelTipoDeComponente = this.correspondenciaDeVistaConTablasEnLaBD.get(tipoComponente);
        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio(tablaDelTipoDeComponente);
        List<Componente> componentesCompatibles = new ArrayList<>();

        for (Componente componente : componentesDeTipo) {
            Boolean esCompatibleConElArmado = this.servicioCompatibilidades.esCompatibleConElArmado(componente, armadoPcDto.obtenerEntidad());
            if (esCompatibleConElArmado) componentesCompatibles.add(componente);
        }


        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatibles);

        return listaDeComponentesDto;
    }

    @Override
    public List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDto(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException {
        String tablaDelTipoDeComponente = this.correspondenciaDeVistaConTablasEnLaBD.get(tipoComponente);

        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio(tablaDelTipoDeComponente, nombreFiltro);

        List<Componente> componentesCompatibles = new ArrayList<>();

        for (Componente componente : componentesDeTipo) {
            Boolean esCompatibleConElArmado = this.servicioCompatibilidades.esCompatibleConElArmado(componente, armadoPcDto.obtenerEntidad());
            if (esCompatibleConElArmado) componentesCompatibles.add(componente);
        }

        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatibles);

        return listaDeComponentesDto;
    }

    private List<ComponenteDto> transformarComponentesADtos(List<Componente> componentesDeTipo) {

        List<ComponenteDto> listaDeComponentesDto = new ArrayList<ComponenteDto>();

        for(Componente c : componentesDeTipo) listaDeComponentesDto.add(new ComponenteDto(c));

        return listaDeComponentesDto;
    }

    // LOS SWITCH DE AGREGAR Y QUITAR PODRIAN ESTAR EN UN SOLO METODO QUE DEPENDIENDO EL PARAMETRO DE AGREGAR O QUITAR EJECUTE LO CORRESPONDIENTE (esto para mantener un solo switch y que no sea tan engorroso)

    @Override
    public ArmadoPcDto agregarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException {

        Componente componenteSolicitado = obtenerComponentePorId(idComponente);

        seExcedeDeLimite(tipoComponente, cantidad, armadoPcDto);

        // determinar un metodo de componente que diga cuando un componente es compatible con el armado

        List<ComponenteDto> perifericosPrecargados = armadoPcDto.getPerifericos();
        ComponenteDto monitorPrecargado = armadoPcDto.getMonitor();


        ComponenteDto componenteSolicitadoDto = new ComponenteDto(componenteSolicitado);
        String precioFormateado = this.servicioPrecios.conversionDolarAPeso(componenteSolicitadoDto.getPrecio());
        componenteSolicitadoDto.setPrecioFormateado(precioFormateado);


        switch(tipoComponente.toLowerCase()){
            case "procesador":{

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("procesador", "motherboard", "coolercpu", "memoriaram", "placadevideo", "almacenamiento", "fuentedealimentacion", "gabinete"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto = new ArmadoPcDto();
                armadoPcDto.setProcesador(componenteSolicitadoDto);
                armadoPcDto.setPerifericos(perifericosPrecargados);
                armadoPcDto.setMonitor(monitorPrecargado);

                break;
            }
            case "motherboard": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("motherboard", "coolercpu", "memoriaram", "placadevideo", "almacenamiento", "fuentedealimentacion", "gabinete"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                ComponenteDto procesadorPrecargado = armadoPcDto.getProcesador();
                armadoPcDto = new ArmadoPcDto();
                armadoPcDto.setPerifericos(perifericosPrecargados);
                armadoPcDto.setMonitor(monitorPrecargado);
                armadoPcDto.setProcesador(procesadorPrecargado);
                armadoPcDto.setMotherboard(componenteSolicitadoDto);

                break;
            }
            case "cooler": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("coolercpu", "fuentedealimentacion", "gabinete"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setCooler(componenteSolicitadoDto);
                armadoPcDto.setFuente(null);
                armadoPcDto.setGabinete(null);

                break;
            }
            case "memoria": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("fuentedealimentacion"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                for (int i = 0; i < cantidad; i++) armadoPcDto.getRams().add(componenteSolicitadoDto);
                armadoPcDto.setFuente(null);

                break;
            }
            case "gpu": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("gpu", "fuentedealimentacion"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setGpu(componenteSolicitadoDto);
                armadoPcDto.setFuente(null);

                break;
            }
            case "almacenamiento": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("fuentedealimentacion"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                for (int i = 0; i < cantidad; i++) armadoPcDto.getAlmacenamiento().add(componenteSolicitadoDto);
                armadoPcDto.setFuente(null);

                break;
            }
            case "fuente": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("fuentedealimentacion"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setFuente(componenteSolicitadoDto);

                break;
            }
            case "gabinete": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("gabinete"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setGabinete(componenteSolicitadoDto);

                break;
            }
            case "monitor": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("monitor"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setMonitor(componenteSolicitadoDto);
                break;
            }
            case "periferico": {
                armadoPcDto.getPerifericos().add(componenteSolicitadoDto);
                break;
            }
        }

        // descuento el stock del componente solicitado con la cantidad
        this.repositorioComponente.descontarStockDeUnComponente(componenteSolicitado.getId(), cantidad);

        return armadoPcDto;
    }

    private Map<Long, Integer> obtenerIdsYCantidadDeComponentesAEliminar(ArmadoPcDto armadoPcDto, Set<String> tiposABorrar) {

        Map<Long, Integer> idYCantidadesAEliminar = new HashMap<>();

        for(ComponenteDto componenteDto : armadoPcDto.getComponentesDto()){
            if (tiposABorrar.contains(componenteDto.getTipoComponente().toLowerCase()))

                if (idYCantidadesAEliminar.containsKey(componenteDto.getId()))
                    idYCantidadesAEliminar.put(componenteDto.getId(), idYCantidadesAEliminar.get(componenteDto.getId()) + 1);
                else
                    idYCantidadesAEliminar.put(componenteDto.getId(), 1);

        }

        return idYCantidadesAEliminar;
    }

    @Override
    public ArmadoPcDto quitarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {

        if (!this.verificarExistenciaDeComponenteEnElArmadoDto(idComponente, armadoPcDto)) throw new QuitarComponenteInvalidoException();

        List<ComponenteDto> perifericosPrecargados = armadoPcDto.getPerifericos();
        ComponenteDto monitorPrecargado = armadoPcDto.getMonitor();

        switch(tipoComponente.toLowerCase()){
            case "procesador": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("motherboard", "coolercpu", "memoriaram", "placadevideo", "almacenamiento", "fuentedealimentacion", "gabinete"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto = new ArmadoPcDto();
                armadoPcDto.setPerifericos(perifericosPrecargados);
                armadoPcDto.setMonitor(monitorPrecargado);

                break;
            }
            case "motherboard": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("coolercpu", "memoriaram", "placadevideo", "almacenamiento", "fuentedealimentacion", "gabinete"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                ComponenteDto procesadorPrecargado = armadoPcDto.getProcesador();
                armadoPcDto = new ArmadoPcDto();
                armadoPcDto.setPerifericos(perifericosPrecargados);
                armadoPcDto.setMonitor(monitorPrecargado);
                armadoPcDto.setProcesador(procesadorPrecargado);

                break;
            }
            case "cooler": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("fuentedealimentacion", "gabinete"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setCooler(null);
                armadoPcDto.setFuente(null);
                armadoPcDto.setGabinete(null);

                break;
            }
            case "memoria": {

                this.eliminarComponenteDeLaListaDeDtosPorId(armadoPcDto.getRams(), idComponente, cantidad);

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("fuentedealimentacion"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setFuente(null);
                break;
            }
            case "gpu": {

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("fuentedealimentacion"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setGpu(null);
                armadoPcDto.setFuente(null);
                break;
            }
            case "almacenamiento": {
                this.eliminarComponenteDeLaListaDeDtosPorId(armadoPcDto.getAlmacenamiento(), idComponente, cantidad);

                Map<Long, Integer> idYCantidadAEliminar = this.obtenerIdsYCantidadDeComponentesAEliminar(armadoPcDto, Set.of("fuentedealimentacion"));

                for (Long id : idYCantidadAEliminar.keySet())
                    this.repositorioComponente.devolverStockDeUnComponente(id, idYCantidadAEliminar.get(id));

                armadoPcDto.setFuente(null);
                break;
            }
            case "fuente": {
                armadoPcDto.setFuente(null);
                break;
            }
            case "gabinete": {
                armadoPcDto.setGabinete(null);
                break;
            }
            case "monitor": {
                armadoPcDto.setMonitor(null);
                break;
            }
            case "periferico": {
                this.eliminarComponenteDeLaListaDeDtosPorId(armadoPcDto.getPerifericos(), idComponente, cantidad);
                break;
            }
        }

        // descuento el stock del componente que quito
        this.repositorioComponente.devolverStockDeUnComponente(idComponente, cantidad);

        return armadoPcDto;
    }

    private Boolean verificarExistenciaDeComponenteEnElArmadoDto(Long idComponente, ArmadoPcDto armadoPcDto) {

        List<ComponenteDto> componentesDelArmado = new ArrayList<>();

        componentesDelArmado.add(armadoPcDto.getProcesador());
        componentesDelArmado.add(armadoPcDto.getMotherboard());
        componentesDelArmado.add(armadoPcDto.getCooler());
        componentesDelArmado.add(armadoPcDto.getGpu());
        componentesDelArmado.add(armadoPcDto.getFuente());
        componentesDelArmado.add(armadoPcDto.getGabinete());
        componentesDelArmado.add(armadoPcDto.getMonitor());
        componentesDelArmado.addAll(armadoPcDto.getRams());
        componentesDelArmado.addAll(armadoPcDto.getAlmacenamiento());
        componentesDelArmado.addAll(armadoPcDto.getPerifericos());

        for(ComponenteDto c : componentesDelArmado) if(c != null && c.getId().equals(idComponente)) return true;

        return false;
    }

    private void eliminarComponenteDeLaListaDeDtosPorId(List<ComponenteDto> componentesDto, Long idComponente, Integer cantidad) throws QuitarStockDemasDeComponenteException {

        List<ComponenteDto> componentesAEliminar = new ArrayList<>();

        for(ComponenteDto componenteDto : componentesDto) if(componenteDto.getId().equals(idComponente) && componentesAEliminar.size() < cantidad) componentesAEliminar.add(componenteDto);

        if(componentesAEliminar.size() >= cantidad && !componentesAEliminar.isEmpty()) for(ComponenteDto componenteAEliminar : componentesAEliminar) componentesDto.remove(componenteAEliminar);

        else throw new QuitarStockDemasDeComponenteException();
    }


    private void seExcedeDeLimite(String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        if ((tipoComponente.equalsIgnoreCase("memoria") && armadoPcDto.getRams().size() + cantidad > 4) ||
                // verificar cantidad de slots de memoria de entidad y cantidad de cada tipo de almacenamiento(SATA y M.2)
                (tipoComponente.equalsIgnoreCase("almacenamiento") && armadoPcDto.getAlmacenamiento().size() + cantidad > 6) ||
                (tipoComponente.equalsIgnoreCase("periferico") && armadoPcDto.getPerifericos().size() + cantidad > 10)) {


            throw new LimiteDeComponenteSobrepasadoEnElArmadoException();
        }
    }

    @Override
    public Boolean sePuedeAgregarMasUnidades(String tipoComponente, ArmadoPcDto armadoPcDto) {

        switch (tipoComponente.toLowerCase()) {

            case "memoria":
                return armadoPcDto.getRams().size() < 4;

            case "almacenamiento":
                return armadoPcDto.getAlmacenamiento().size() < 6;

            case "periferico":
                return armadoPcDto.getPerifericos().size() < 10;

            case "procesador":
            case "motherboard":
            case "cooler":
            case "gpu":
            case "fuente":
            case "gabinete":
            case "monitor":
                return false;

            default:
                // Si no conocemos el tipo, por las dudas no permitimos más
                return false;
        }
    }

    @Override
    public Boolean armadoCompleto(ArmadoPcDto armadoPcDto) {
        return armadoPcDto.getMotherboard() != null
                && armadoPcDto.getProcesador() != null
                && armadoPcDto.getCooler() != null
                && armadoPcDto.getGabinete() != null;
    }

}
