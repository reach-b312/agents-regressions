package agents.regressions;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.io.BufferedReader;
//import java.io.File; //Temp
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class MainAgent extends Agent {
    protected void setup() {
        System.out.println("Agente "+getLocalName()+" iniciado.");
        String tipoServicio = determinarTipoServicio("dataset.csv");
        addBehaviour(new TickerBehaviour(this, 6000) {
            protected void onTick() {
                // Leer el archivo dataset.csv y determinar qué tipo de servicio solicitar
                //System.out.println(new File(".").getAbsolutePath());



                // Buscar el agente que ofrece el servicio necesario usando yellowPages
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(tipoServicio);
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        // Enviar mensaje ACL al agente de servicio encontrado
                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(result[0].getName());
                        msg.setContent("Solicitud de servicio de regresión: " + tipoServicio);
                        myAgent.send(msg);
                        System.out.println("Mensaje enviado al agente de servicio: " + result[0].getName());
                    } else {
                        System.out.println("No se encontró ningún agente que ofrezca el servicio: " + tipoServicio);
                    }
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    private String DEPRECATEDdeterminarTipoServicio(String archivo) {
        // Implementación de la lógica para determinar el tipo de servicio basado en el archivo
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String line = br.readLine(); // Leer la primera línea para determinar el tipo de datos
            String[] headers = line.split(",");
            if (headers.length == 2) {
                return "SLR"; // Regresión Lineal Simple
            } else if (headers.length > 2) {
                // Verificar si la última columna contiene un número o categorías
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    try {
                        Double.parseDouble(data[data.length - 1]);
                        return "MLR"; // Regresión Lineal Múltiple
                    } catch (NumberFormatException e) {
                        return "LOG"; // Regresión Logística
                    }
                }
            }
            // Si hay una tercera columna con un solo número, asumir regresión polinomial
            return "POLY"; // Regresión Polinomial
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // En caso de error
    }
    private String determinarTipoServicio(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String line = br.readLine(); // Leer la primera línea para obtener los encabezados
            int numColumns = line.split(",").length;
            boolean isCategorical = false;
            boolean isPolynomial = false;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Verificar si la última columna es categórica
                if (data[data.length - 1].matches("0|1")) {
                    isCategorical = true;
                    break;
                }
                // Verificar si la última columna es un número discreto (grado del polinomio)
                try {
                    int degree = Integer.parseInt(data[data.length - 1]);
                    if (degree > 0 && degree < 10) {
                        isPolynomial = true;
                        break;
                    }
                } catch (NumberFormatException e) {
                    // No es un número, continuar con la siguiente línea
                }
            }

            if (isCategorical) {
                return "LOG"; // Regresión Logística
            } else if (isPolynomial) {
                return "POLY"; // Regresión Polinomial
            } else if (numColumns == 2) {
                return "SLR"; // Regresión Lineal Simple
            } else {
                return "MLR"; // Regresión Lineal Múltiple
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // En caso de error
    }

}
