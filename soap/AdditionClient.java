import calculette.AdditionServiceStub;


// Commande pour générer le code : : wsdl2java.sh -uri http://localhost:8080/axis2/services/AdditionService?wsdl -d adb
public class AdditionClient {

    public static void main(String[] args) throws Exception {
        AdditionServiceStub stub = new AdditionServiceStub(
                "http://localhost:8080/axis2/services/AdditionService");

        AdditionServiceStub.Additionner requete = new AdditionServiceStub.Additionner();
        requete.setEntier1(12);
        requete.setEntier2(30);

        AdditionServiceStub.AdditionnerResponse reponse = stub.additionner(requete);

        System.out.println("Resultat = " + reponse.getResultat1());
    }
}
