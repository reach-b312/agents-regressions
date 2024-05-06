package agents.regressions;
// MLRagent.java
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import regressions.MultipleLinearRegression;

public class MLRagent extends Agent {
    protected void setup() {
        System.out.println("Agente "+getLocalName()+" iniciado.");

        // Registrar el servicio MLR en yellowPages
        ServiceDescription sd = new ServiceDescription();
        sd.setType("MLR");
        sd.setName(getLocalName());
        registerService(sd);

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                // Esperar y procesar solicitudes de regresión múltiple
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    // Mensaje recibido. Procesar la solicitud de regresión múltiple
                    System.out.println("Solicitud de regresión MLR recibida de " + msg.getSender().getName());
                    // Convertir las columnas a una matriz X y un vector y

                    /*
                    String[] lines = msg.getContent().split("\\n");
                    double[][] X = new double[lines.length][];
                    double[] y = new double[lines.length];
                    for (int i = 0; i < lines.length; i++) {
                        String[] data = lines[i].split(", ");
                        X[i] = new double[data.length - 1];
                        for (int j = 0; j < data.length - 1; j++) {
                            X[i][j] = Double.parseDouble(data[j]);
                        }
                        y[i] = Double.parseDouble(data[data.length - 1]);
                    }
                    */
                    ArrayList<String> lines = new ArrayList<String>();
                    String csvFile = "dataset.csv";
                    String line = "";
                    String cvsSplitBy = ",";

                    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                        while ((line = br.readLine()) != null) {
                            lines.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String[] data = lines.get(0).split(cvsSplitBy);
                    double[][] X = new double[lines.size()][data.length-1];
                    double[] y = new double[lines.size()];
                    for (int i = 0; i < lines.size(); i++) {
                        data = lines.get(i).split(cvsSplitBy);
                        X[i] = new double[data.length - 1];
                        for (int j = 0; j < data.length - 1; j++) {
                            X[i][j] = Double.parseDouble(data[j]);
                        }
                        y[i] = Double.parseDouble(data[data.length - 1]);
                    }
                    // Llamar al método fit de la clase externa
                    MultipleLinearRegression MLR = new MultipleLinearRegression(X, y);
                    double [] pesos = MLR.fit();
                    //double[] pesos = Regresion.fit(X, y);
                    // Calcular las predicciones y escribirlas en output.csv
                    try (FileWriter writer = new FileWriter("output.csv")) {
                        //writer.write("X,y,prediccion\n");
                        for (int i = 0; i < y.length; i++) {
                            double prediccion = 0;
                            for (int j = 0; j < pesos.length; j++){
                                prediccion+= pesos[j]*X[i][j];
                            }
                            /*double prediccion = pesos[0]; // Intercepto
                            for (int j = 0; j < X[i].length-1; j++) {
                                prediccion += pesos[j + 1] * X[i][j]; // Sumar los productos de los pesos y las variables independientes
                            }*/
                            writer.write(String.join(",", vectorToString(X[i])) + "," + y[i] + "," + prediccion + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Enviar los resultados al agente que solicitó el servicio
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Resultados de regresión MLR escritos en output.csv");
                    myAgent.send(reply);
                    System.out.print("Respuesta enviada> Resultados de regresión MLR escritos en output.csv");
                } else {
                    block();
                }
            }
        });
    }

    private void registerService(ServiceDescription sd) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private String vectorToString(double[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
