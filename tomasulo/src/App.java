
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class App{
    public int arraysize = 0;
    public String[] instructionMemory;
    public ArrayList<Label> labellist = new ArrayList<Label>();
    
    LinkedList<Instruction> InstructionTable = new LinkedList<>();
    LinkedList<Instruction> instructionQueue = new LinkedList<>();
    LinkedList<Instruction> instructionQueueUnedited = new LinkedList<>();
    Queue<Instruction> awzeenwriteback;

    public Reservation addsubReservation;
    public Reservation muldivReservation;
    public Reservation loadReservation;
    public Reservation storeReservation;
    public static FileReg intRegfile;
    public static FileReg floatRegfile;
    public static IntCache intCache;
    public static FloatCache floatCache;

    public static int addTimer;
    public static int subTimer;
    public static int mulTimer;
    public static int divTimer;
    public static int loadTimer = 1;
    public static int storeTimer = 1;
    public static int branchTimer = 1;
    public static int addI = 1;
    public static int cyclenumber = 1;
    public static int index = 1;
    public static String operation;
    public static Instruction InstructionToIssue;

    boolean DidIIssue = false;
    boolean stillprinting = true;
    boolean stillprintingbegad = true;
    boolean stopIssue = false;
    boolean NoMoreInstructions= false;
    boolean FullRegStation= false;



    
    public void eshtaghal() throws IOException {
	LoadInstructions();
    CreateResStations();
    while(stillprintingbegad == true){
    issue();
    execute();
    writeBack();
    Print();
    cyclenumber++;
    if (!stopIssue){
        index++;
    }
    checkifdone();
    }
}

public void updateInstructionTable(Instruction updatedInstruction, String update, int cycle) {
    for (int i = 0; i < InstructionTable.size(); i++) {
        Instruction inst = InstructionTable.get(i);
        if (inst.equals(updatedInstruction)) { // You may need to define the equals method in your Instruction class
            switch (update) {
                case "issue":
                    inst.issueCycle = cycle;
                    break;
                case "start":
                    inst.startCycle = cycle + 1;
                    break;
                case "end":
                    inst.endCycle = cycle -1;
                    break;
                case "write":
                    inst.writeCycle = cycle;
                    break;
            
                default:
                    break;
            }
            break;
        }
    }
}


public void InstructionTable(){
        System.out.println("----------------------------------------------------------------------------");

        System.out.printf("| %-20s | %-10s | %-10s | %-10s | %-10s |\n",
                "Instruction","Issue", "Start Exec", "End Exec", "Write Back");
        System.out.println("----------------------------------------------------------------------------");        
        
    for (Instruction inst: InstructionTable){
        System.out.printf("| %-20s | %-10d | %-10d | %-10d | %-10d |\n",
                            inst.getInstruction(), inst.issueCycle, inst.startCycle, inst.endCycle, inst.writeCycle);
    }
        System.out.println("----------------------------------------------------------------------------");        

}
public void Print(){
	System.out.println("____________________________________________________________________________________________________________________________________________________");
    System.out.println("");
    System.out.println("");

     System.out.println("                                                   (  CYCLE NUMBER "+ cyclenumber+ "  )");
     System.out.println("");
     System.out.println("");
     System.out.println("");
     System.out.println("");

        if (index <= instructionQueue.size()){
            if (!stopIssue){
                System.out.println("Instruction to be issued: " + InstructionToIssue);
            }
        }
        if(FullRegStation){
            System.out.println("No empty reservation station, can't issue!");
            System.out.println("");
            System.out.println("");
        }
        if (NoMoreInstructions) {
             System.out.println("Instruction queue is empty, we have issued everything!");
             System.out.println("");
             System.out.println("");

        }


        System.out.println("Instruction Queue:");
        for (Instruction printInstruction : instructionQueueUnedited) {
            if(printInstruction.operation.equals("L.D")){
                    System.out.println(printInstruction.operation + " " + printInstruction.type + " "+ printInstruction.destination + " " + printInstruction.target);

            }
            else if(printInstruction.operation.equals("S.D")){
                    System.out.println(printInstruction.operation + " " + printInstruction.type + " "+ printInstruction.src + " " + printInstruction.target);
            }
            else{
                System.out.println(printInstruction.operation + " " + printInstruction.type + " "+ printInstruction.destination + " " + printInstruction.src + " " + printInstruction.target);
            }
            
        }
        System.out.println("");
        System.out.println("");
        System.out.println("");

       System.out.println("Int Cache: \n");
        System.out.println(intCache);
        System.out.println("");
        System.out.println("");
        System.out.println("Float Cache : \n");
        System.out.println(floatCache);
      System.out.println("");
        System.out.println("");
        System.out.println("Int Register File: \n");
        System.out.println(intRegfile);
        System.out.println("");
        System.out.println("");
        System.out.println("Float Register File: \n");
        System.out.println(floatRegfile);
        System.out.println("");
        System.out.println("");
        
        System.out.println("TO WRITE BACK IN FIFO ORDER: ");
        for (Instruction printInstruction : awzeenwriteback) {
            System.out.print(printInstruction);
        }
         System.out.println("");
        System.out.println("");
        System.out.println("");
    printReservationStationTable(addsubReservation, "ADD/SUB Reservation Station");
    printReservationStationTable(muldivReservation, "MUL/DIV Reservation Station");
    printReservationStationTable(loadReservation, "Load Reservation Station");
    printReservationStationTable(storeReservation, "Store Reservation Station");
    System.out.println("INSTRUCTION TABLE: \n");
    System.out.println("");
    InstructionTable();
}


	public void LoadInstructions() throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader("src/instructions.txt"))) {
        String currentInstruction = "";

        // Enable marking with a large enough limit
        reader.mark(4096);

        // First loop to get the array size
        while ((currentInstruction = reader.readLine()) != null) {
            arraysize++;
        }

        // Reset the reader to the marked position
        reader.reset();
        instructionQueue = new LinkedList<>();
        instructionQueueUnedited = new LinkedList<>();
        awzeenwriteback = new LinkedList<>();
        instructionMemory = new String[arraysize];
        Instruction thisInstruction = new Instruction();
        Instruction copyInstruction = new Instruction();
        int readerindex = 0;

        // Second loop to process instructions
        while ((currentInstruction = reader.readLine()) != null) {
            String[] field = currentInstruction.split(" ");
            String operation, destination, src, target;
            String address;
            char type;
            readerindex++;
            String branchlabel;
            switch (field[0]) {
                //REGISTER OPERATIONS \/\/\/\/\/\/\/\/\/\/\/\/\/\/
                case "ADD.D":
                    operation = "ADD.D";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3].substring(1);
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type, operation,destination, src, target);
                    break;
                case "DADD":
                    operation = "DADD";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3].substring(1);
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type, operation,destination, src, target);
                    break;
                case "SUB.D":
                    operation = "SUB.D";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3].substring(1);
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type, operation, destination, src, target);
                    break;
                case "DSUB":
                    operation = "DSUB";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3].substring(1);
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type, operation, destination, src, target);
                    break;
                case "MUL.D":
                    operation = "MUL.D";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3].substring(1);
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type, operation, destination ,src, target);
                    break;
                case "DIV.D":
                    operation = "DIV.D";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3].substring(1);
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type,operation, destination, src, target);
                    break;
                 case "DMUL":
                    operation = "DMUL";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3].substring(1);
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type, operation, destination ,src, target);
                    break;
                case "DDIV":
                    operation = "DDIV";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3].substring(1);
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type,operation, destination, src, target);
                    break;
                //REGISTER OPERATIONS ^^^^^^^^^^^^^^^^


                //IMMEDIATE OPERATIONS \/\/\/\/\/\/\/\/\/\/\/\/\/\/
                case "ADDI":
                    operation = "ADDI";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3];
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type,operation, destination, src, target);
                    copyInstruction = new Instruction(type,operation, destination, src, target);
                    break;
                case "SUBI":
                    operation = "SUBI";
                    destination = field[1].substring(1);
                    src = field[2].substring(1);
                    target = field[3];
                    type = field[1].charAt(0); //R or F
                    thisInstruction = new Instruction(type, operation, destination, src, target);
                    copyInstruction = new Instruction(type,operation, destination, src, target);
                    break;
                //IMMEDIATE OPERATIONS ^^^^^^^^^^^^^^^^

                //LOAD OPERATIONNNSSS \/\/\/\/\/\/\/\/\/\/\/\/\/
                case "L.D":
                    operation = "L.D";
                    type= field[1].charAt(0);
                    destination= field[1].substring(1);
                    address= field[2];
                    //da by print elly ablyy
                    //System.out.println("Destination"+ thisInstruction.destination);
                    //System.out.println("Addres= "+ thisInstruction.target);
                    thisInstruction= new Instruction(type, operation, destination, address);
                    copyInstruction = new Instruction(type,operation, destination, address);
                    break;
                //LOAD OPERATIONS ^^^^^^^^^^^^^^^^^^^^^^^^^^^
                
                //STORE OPERATIONSSSS \/\/\/\/\/\\/\\/\/\/\/
                case "S.D":
                    operation  = "S.D";
                    type= field[1].charAt(0);
                    src= field[1].substring(1);
                    address= field[2];
                    thisInstruction= new Instruction(type, operation, src, address);
                    copyInstruction = new Instruction(type,operation, src, address);
                    break;
                //STORE OPERTAIONS ^^^^^^^^^^^^^^^^^^^^^^^^^    
                case "BNEZ":
                operation = "BNEZ";
                type= field[1].charAt(0);
                src= field[1].substring(1);
                branchlabel = field[2];
                thisInstruction = new Instruction(operation, type, src, "B",branchlabel);
                copyInstruction = new Instruction(operation, type, src,"B",  branchlabel);
                break;
                case "BNE":
                    operation = "BNE";
                    type = field[1].charAt(0); // Extract type (e.g., 'R')
                    String register1 = field[1].substring(1); // First register (R1)
                    String register2 = field[2].substring(1); // Second register (R2)
                    branchlabel = field[3]; // Branch target (LOOP)
                    thisInstruction = new Instruction(operation, type, "B", register1, register2, branchlabel);
                    copyInstruction = new Instruction(operation, type, "B", register1, register2, branchlabel);
                    break;
                case "BEQ":
                    operation = "BEQ";
                    type = field[1].charAt(0); // Extract type (e.g., 'R')
               String         register11 = field[1].substring(1); // First register (R1)
                String     register22 = field[2].substring(1); // Second register (R2)
                    branchlabel = field[3]; // Branch target (LOOP)
                    thisInstruction = new Instruction(operation, type, "B", register11, register22, branchlabel);
                    copyInstruction = new Instruction(operation, type, "B", register11, register22, branchlabel);
                    break;


                
                default:
                    Label newlabel = new Label(field[0], readerindex);
                    labellist.add(newlabel);
                    thisInstruction = new Instruction('x', "imalabel", "x", "x", "x");
                    copyInstruction = new Instruction('x', "imalabel", "x", "x", "x");

            }
            
            instructionQueue.add(thisInstruction);
       
            instructionQueueUnedited.add(copyInstruction);

            System.out.println("operation: " + thisInstruction.operation);
            System.out.println("destination: " + thisInstruction.destination);
            if(thisInstruction.operation == "BNE" || thisInstruction.operation == "BEQ") {
            System.out.println("src: " + thisInstruction.register1);
            System.out.println("target: " + thisInstruction.register2);
            System.out.println("_________________________________________________");
        }
            else {
            	System.out.println("src: " + thisInstruction.src);
                System.out.println("target: " + thisInstruction.target);
                System.out.println("_________________________________________________");
            }
            	
            }
        System.out.println("InstructionQueue:");
        for (Instruction printInstruction : instructionQueueUnedited) {
              if(printInstruction.operation.equals("L.D")){
                    System.out.println(printInstruction.operation + " " + printInstruction.type + " "+ printInstruction.destination + " " + printInstruction.target);

            }
            else if(printInstruction.operation.equals("S.D")){
                    System.out.println(printInstruction.operation + " " + printInstruction.type + " "+ printInstruction.src + " " + printInstruction.target);
            }
            else{
                System.out.println(printInstruction.operation + " " + printInstruction.type + " "+ printInstruction.destination + " " + printInstruction.src + " " + printInstruction.target);
            }
            
        }
        System.out.println("_________________________________________________");

    } catch (IOException e) {
        e.printStackTrace();
    }
}
public void CreateResStations() {
    Scanner scanner = new Scanner(System.in);

    System.out.print("Enter The size of the ADD/SUB reservation station: ");
    String ADDstationsize = scanner.nextLine();
    addsubReservation = new Reservation("A", Integer.parseInt(ADDstationsize));

    System.out.print("Enter The size of the MUL/DIV reservation station: ");
    String MULstationsize = scanner.nextLine();
    muldivReservation = new Reservation("M", Integer.parseInt(MULstationsize));

    System.out.print("Enter The size of the Load reservation station: ");
    String Loadstationsize = scanner.nextLine();
    loadReservation = new Reservation("L", Integer.parseInt(Loadstationsize));

    System.out.print("Enter The size of the store reservation station: ");
    String Storestationsize = scanner.nextLine();
    storeReservation = new Reservation("S", Integer.parseInt(Storestationsize));

    System.out.print("Enter the latency of the ADD: ");
    addTimer = Integer.parseInt(scanner.nextLine());

    System.out.print("Enter the latency of the SUB: ");
    subTimer = Integer.parseInt(scanner.nextLine());

    System.out.print("Enter the latency of the MUL: ");
    mulTimer = Integer.parseInt(scanner.nextLine());

    System.out.print("Enter the latency of the DIV: ");
    divTimer = Integer.parseInt(scanner.nextLine());

    scanner.close();

    System.out.println("Add Timer: " + addTimer);
    System.out.println("Mul Timer: " + mulTimer);
    System.out.println("_________________________________________________");
}

private void printReservationStationTable(Reservation reservation, String title) {
    
    System.out.println(title);
    if (reservation.label.equals("A") || reservation.label.equals("M") || reservation.label.equals("B")) {
        System.out.println("----------------------------------------------------------------------------------------------------");

        System.out.printf("| %-10s | %-10s | %-6s | %-10s | %-10s | %-10s | %-10s | %-10s |\n",
                "Timer","Label", "Busy", "Operation", "Vj", "Vk", "Qj", "Qk");
        System.out.println("----------------------------------------------------------------------------------------------------");        
        
    }
     if (reservation.label.equals("L") ) {
        System.out.println("-------------------------------------------------");        

        System.out.printf("| %-10s | %-10s | %-6s | %-10s | \n",
                "Timer","Label", "Busy", "Target");
        System.out.println("-------------------------------------------------");        

    }
    if (reservation.label.equals("S") ) {
        System.out.println("--------------------------------------------------------------------------");        

        System.out.printf("| %-10s | %-10s | %-6s | %-10s | %-10s | %-10s |\n",
                "Timer","Label", "Busy", "Target", "Vj", "Qj");
        System.out.println("--------------------------------------------------------------------------");        

    }


    for (int i = 0; i < reservation.size; i++) {
        

        if (reservation.reservationStation[i]!=null){
            if (reservation.reservationStation[i].getLabel()== null){
            reservation.reservationStation[i].setLabel(" ");
            }
            //ADD SUB MUL DIV RESERVATION STATION
            if(reservation.label.equals("A") || reservation.label.equals("M") || reservation.label.equals("B")){
                if (reservation.reservationStation[i].getTimer()== -1){
                            
                    
                }
                else if (reservation.reservationStation[i].getTimer()== 0 && reservation.reservationStation[i].StartExec==false){
                            System.out.printf("| %-10s | %-10s | %-6d | %-10s | %-10s | %-10s | %-10s | %-10s |\n",
                            " ", reservation.reservationStation[i].getLabel() ,reservation.reservationStation[i].getBusy(),
                            reservation.reservationStation[i].getOperation(), reservation.reservationStation[i].Vj, reservation.reservationStation[i].Vk,
                            reservation.reservationStation[i].Qj, reservation.reservationStation[i].Qk);
                    
                }
                else {
                    System.out.printf("| %-10d | %-10s | %-6d | %-10s | %-10s | %-10s | %-10s | %-10s |\n",
                    reservation.reservationStation[i].getTimer(),reservation.reservationStation[i].getLabel(), reservation.reservationStation[i].getBusy(),
                        reservation.reservationStation[i].getOperation(), reservation.reservationStation[i].Vj, reservation.reservationStation[i].Vk,
                        reservation.reservationStation[i].Qj, reservation.reservationStation[i].Qk);
                
                    }
            }

            //LOAD RESERVATION STATION
            if(reservation.label.equals("L")){
                if (reservation.reservationStation[i].getTimer()== -1){
                            // System.out.printf("| %-10s | %-10s | %-6d | %-10s |\n",
                            //  " ", reservation.reservationStation[i].getLabel() ,reservation.reservationStation[i].getBusy(),
                            //  reservation.reservationStation[i].target);
                    
                }
                else if (reservation.reservationStation[i].getTimer()== 0 && reservation.reservationStation[i].StartExec==false){
                            System.out.printf("| %-10s | %-10s | %-6d | %-10s |\n",
                             " ", reservation.reservationStation[i].getLabel() ,reservation.reservationStation[i].getBusy(),
                             reservation.reservationStation[i].target);
                    
                }
                else {
                    System.out.printf("| %-10d | %-10s | %-6d | %-10s |\n",
                    reservation.reservationStation[i].getTimer(),reservation.reservationStation[i].getLabel(), reservation.reservationStation[i].getBusy(),
                         reservation.reservationStation[i].target);
                
                    }
            }

            //STORE RESERVATION STATION
            if(reservation.label.equals("S")){
                if (reservation.reservationStation[i].getTimer()== -1){
                        //     System.out.printf("| %-10s | %-10s | %-6d | %-10s | %-10s | %-10s |\n",
                        //      " ",reservation.reservationStation[i].getLabel() , reservation.reservationStation[i].getBusy(),
                        //    reservation.reservationStation[i].target, reservation.reservationStation[i].Vj, reservation.reservationStation[i].Qj);
                    
                }
                else if (reservation.reservationStation[i].getTimer()== 0 && reservation.reservationStation[i].StartExec==false){
                            System.out.printf("| %-10s | %-10s | %-6d | %-10s | %-10s | %-10s |\n",
                             " ",reservation.reservationStation[i].getLabel() , reservation.reservationStation[i].getBusy(),
                           reservation.reservationStation[i].target, reservation.reservationStation[i].Vj, reservation.reservationStation[i].Qj);
                    
                }
                else {
                            System.out.printf("| %-10d | %-10s | %-6d | %-10s | %-10s | %-10s |\n",
                      reservation.reservationStation[i].getTimer(),reservation.reservationStation[i].getLabel(), reservation.reservationStation[i].getBusy(),
                    reservation.reservationStation[i].target, reservation.reservationStation[i].Vj, reservation.reservationStation[i].Qj);
                    
                    }
            }

         }
     }
    if(reservation.label.equals("A") || reservation.label.equals("M") || reservation.label.equals("B")){
        System.out.println("----------------------------------------------------------------------------------------------------");

    }
     if(reservation.label.equals("L")){
        System.out.println("-------------------------------------------------");        
     }
      if(reservation.label.equals("S")){
        System.out.println("--------------------------------------------------------------------------");        

     }



     System.out.println("");
     System.out.println("");
    System.out.println("");


            
    
}

 
public void issue(){
        //check if there is an empty reservation station
        if (index > instructionQueue.size()){
            NoMoreInstructions= true;
            return;
        }
        //if there is an empty reservation station, issue the instruction to it
        
        if(stopIssue){
            return;
        }
        else{
            if(!stillprinting){
                stillprinting = true;
            }
        Instruction InstructionToIssue2 = new Instruction();
        InstructionToIssue2 = instructionQueue.get(index-1);
        InstructionToIssue = InstructionToIssue2;

        operation = InstructionToIssue.getOperation();
        char type = (InstructionToIssue.type == Type.R)?'R': 'F'; 
        
        Instruction copyInstruction = new Instruction();
        //System.out.println("instructiontoissue: "+InstructionToIssue);
        //System.out.println("OPERATIONNNNNNN"+InstructionToIssue.operation + "LABELLLL "+InstructionToIssue.getLabel());
        switch (operation){
            case "ADD.D":
            	copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "SUB.D":
                copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "DADD":
            	copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "DSUB":
                copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "MUL.D":
                  copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = muldivReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "DIV.D":
                  copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = muldivReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "DMUL":
                  copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = muldivReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "DDIV":
                  copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = muldivReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "L.D":
                  copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination, InstructionToIssue.target);
                DidIIssue = loadReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "S.D":
                copyInstruction = new Instruction(type, InstructionToIssue.operation,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = storeReservation.issueInstruction(copyInstruction, cyclenumber);
                 if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "BNEZ":
                copyInstruction = new Instruction(InstructionToIssue.operation, type,InstructionToIssue.src, "B",InstructionToIssue.branchlabel);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);
                if(DidIIssue){
                    stopIssue = true;
                    copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
            break;
            case "BNE":
                copyInstruction = new Instruction(InstructionToIssue.operation,type,"B",InstructionToIssue.register1,InstructionToIssue.register2,InstructionToIssue.branchlabel);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);

                if (DidIIssue) {
                    stopIssue = true;
                    copyInstruction.issueCycle = cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "BEQ":
                copyInstruction = new Instruction(InstructionToIssue.operation,type,"B",InstructionToIssue.register1,InstructionToIssue.register2,InstructionToIssue.branchlabel);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);

                if (DidIIssue) {
                    stopIssue = true;
                    copyInstruction.issueCycle = cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;

            case "SUBI":
                copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);
                if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "ADDI":
                copyInstruction = new Instruction(type, InstructionToIssue.operation, InstructionToIssue.destination,InstructionToIssue.src,InstructionToIssue.target);
                DidIIssue = addsubReservation.issueInstruction(copyInstruction, cyclenumber);
                if(DidIIssue){
                     copyInstruction.issueCycle= cyclenumber;
                    InstructionTable.add(copyInstruction);
                    updateInstructionTable(copyInstruction, "issue", cyclenumber);
                }
                break;
            case "imalabel":
                DidIIssue = false;
                index++;
                issue();
                break;
        }

        

        if (DidIIssue == true){
            //instructionQueue.get(0);
            copyInstruction.setBusy(1); 
            FullRegStation=false;
        }
        else if (DidIIssue == false && !operation.equals("imalabel")){
            FullRegStation=true;
            index--;
        }
        
    }
       
        //if there is no empty reservation station, do nothing
    }
    public void checker(Instruction instructiontoissueeltanya){
        char labelCheck;
        if (instructiontoissueeltanya.getLabel() != null && !instructiontoissueeltanya.getLabel().isEmpty()) {
            labelCheck = instructiontoissueeltanya.getLabel().charAt(0);
        } else {
            // Handle the case when getLabel() is null or empty
            labelCheck = ' '; // or some default value
        }
        //System.out.println("EH EL LABEL DAAAAA???" + labelCheck);

         if(instructiontoissueeltanya.type == Type.R) //look at the int reg file
                {   //IF ARITHMETIC OPERATION\/\/\/\/\/\/\/\/\/

                    if (labelCheck=='A' || labelCheck=='M'){
                    	//CASE1: LESA MA3MOLAHA ISSUE (Q'S ARE EMPTY) TABEE3Y HATDAWAR 3ALA ITS OPERANDS FEL REG FILE 
                    	//CASE2: GRABBED THE OPERANDS FROM THE BUS SO WILL START EXECUTION NEXT CYCLE (DON'T LOOK AT REG FILE)

                    	if(instructiontoissueeltanya.Qj.equals("") && instructiontoissueeltanya.Qk.equals("")) {

                    		//IF ARITHMETIC WE NEED BOTH (VJ, VK) AND (QJ, QK)
                    		//1- EXTRACT SRC, TARGET, DEST
                    		int src = Integer.parseInt(instructiontoissueeltanya.src);
                    		int target = Integer.parseInt(instructiontoissueeltanya.target);
                    		int destination = Integer.parseInt(instructiontoissueeltanya.destination);
                    		
                    		//2- GO TO INT REG FILE WITH SRC 
                    		//3- CHECK ON FILE[SRC].QI 
                    		//4- IF 0 THEN SET QJ = 0 AND VJ = VALUE
                    		//5- BUT IF NOT? SET QJ = QI AND THAT IS IT
                    		if(intRegfile.registers[src].Qi.equals("0")) {
                    			instructiontoissueeltanya.Qj = "0";
                    			instructiontoissueeltanya.Vj = (double) intRegfile.registers[src].valueR;
                    		}
                    		else {
                    			instructiontoissueeltanya.Qj = intRegfile.registers[src].Qi;
                    		}
                    		
                    		//6- GO TO INT REG FILE WITH TARGET 
                    		//7- CHECK ON FILE[TARGET].QI 
                    		//8- IF 0 THEN SET QK = 0 AND VK = VALUE
                    		//9- BUT IF NOT? SET QK = QI AND THAT IS IT
                            if (!instructiontoissueeltanya.operation.equals("ADDI") && !instructiontoissueeltanya.operation.equals("SUBI")){

                                if(intRegfile.registers[target].Qi.equals("0")) {
                                    instructiontoissueeltanya.Qk = "0";
                                    instructiontoissueeltanya.Vk = (double) intRegfile.registers[target].valueR;
                                }
                                else {
                                    instructiontoissueeltanya.Qk = intRegfile.registers[target].Qi;
                                }

                             }
                             else{
                                  instructiontoissueeltanya.Vk = Double.parseDouble(instructiontoissueeltanya.target) ;
                             }
                    		
                    		//AFTER WE TOOK OUR OPERANDS FROM THE REGISTER FILE WE THEN PUT OUR LABEL
                    		intRegfile.registers[destination].Qi = instructiontoissueeltanya.label;
                    		
                    	}
                    	//FINALLY (IN BOTH CASES) WE CHECK IF THE TWO OPERANDS ARE READY 
                    	//IF YES THEN: 1- SET TIMER  2- SET STARTEXEC  3- ADD IT TO AWZEEN 
                    	if(instructiontoissueeltanya.Qj.equals("0") && instructiontoissueeltanya.Qk.equals("0")) {
                    		 if(labelCheck == 'A'){
                                if(instructiontoissueeltanya.operation.equals("SUB.D") || instructiontoissueeltanya.operation.equals("DSUB")){
                                    instructiontoissueeltanya.setTimer(subTimer);
                                }
                                else if(instructiontoissueeltanya.operation.equals("ADD.D") || instructiontoissueeltanya.operation.equals("DADD")){
                                    instructiontoissueeltanya.setTimer(addTimer);
                                    
                                }
                                else if(instructiontoissueeltanya.operation.equals("ADDI") ){
                                    instructiontoissueeltanya.setTimer(addI);
                                }
                            }
                            else if(labelCheck == 'M'){
                                if(instructiontoissueeltanya.operation.equals("MUL.D") || instructiontoissueeltanya.operation.equals("DMUL")){
                                    instructiontoissueeltanya.setTimer(mulTimer);
                                }
                                else if(instructiontoissueeltanya.operation.equals("DIV.D") || instructiontoissueeltanya.operation.equals("DDIV")){
                                     instructiontoissueeltanya.setTimer(divTimer);
                                } 
                            }
                    		instructiontoissueeltanya.StartExec = true;
                            instructiontoissueeltanya.startCycle= cyclenumber;
                            updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                    		awzeenwriteback.add(instructiontoissueeltanya);
                    	}
                    	
                    	
                    	
                    }else if (labelCheck == 'B') {
                        // Differentiate between BNEZ and BNE
                        if (instructiontoissueeltanya.operation.equals("BNEZ")) {
                            // Handle BNEZ (single register and a branch label)
                            if (instructiontoissueeltanya.Qj.equals("")) {
                                // Extract the source register
                                int src = Integer.parseInt(instructiontoissueeltanya.src);
                                // Check the integer register file for readiness
                                if (intRegfile.registers[src].Qi.equals("0")) {
                                    instructiontoissueeltanya.Qj = "0";
                                    instructiontoissueeltanya.Vj = (double) intRegfile.registers[src].valueR;
                                } else {
                                    instructiontoissueeltanya.Qj = intRegfile.registers[src].Qi;
                                }
                            }

                            // Start execution if Qj is ready
                            if (instructiontoissueeltanya.Qj.equals("0")) {
                                instructiontoissueeltanya.setTimer(branchTimer);
                                instructiontoissueeltanya.StartExec = true;
                                instructiontoissueeltanya.startCycle = cyclenumber;
                                updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                                awzeenwriteback.add(instructiontoissueeltanya);
                            }
                        } else if (instructiontoissueeltanya.operation.equals("BNE") ||(instructiontoissueeltanya.operation.equals("BEQ")))  {
                            // Handle BNE (two registers and a branch label)
                            if (instructiontoissueeltanya.Qj.equals("") && instructiontoissueeltanya.Qk.equals("")) {
                                // Extract both registers
                                int reg1 = Integer.parseInt(instructiontoissueeltanya.register1);
                                int reg2 = Integer.parseInt(instructiontoissueeltanya.register2);

                                // Check the integer register file for readiness of register1
                                if (intRegfile.registers[reg1].Qi.equals("0")) {
                                    instructiontoissueeltanya.Qj = "0";
                                    instructiontoissueeltanya.Vj = (double) intRegfile.registers[reg1].valueR;
                                } else {
                                    instructiontoissueeltanya.Qj = intRegfile.registers[reg1].Qi;
                                }

                                // Check the integer register file for readiness of register2
                                if (intRegfile.registers[reg2].Qi.equals("0")) {
                                    instructiontoissueeltanya.Qk = "0";
                                    instructiontoissueeltanya.Vk = (double) intRegfile.registers[reg2].valueR;
                                } else {
                                    instructiontoissueeltanya.Qk = intRegfile.registers[reg2].Qi;
                                }
                            }

                            // Start execution if both Qj and Qk are ready
                            if (instructiontoissueeltanya.Qj.equals("0") && instructiontoissueeltanya.Qk.equals("0")) {
                                instructiontoissueeltanya.setTimer(branchTimer);
                                instructiontoissueeltanya.StartExec = true;
                                instructiontoissueeltanya.startCycle = cyclenumber;
                                updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                                awzeenwriteback.add(instructiontoissueeltanya);
                            }
                        }
                    }

                    //ARITMETIC ^^^^^^^^^^^^^^^^^^^^^

                    //if LOAD/ STORE  INSTRUCTIONS \/\/\/\/\/\/\/\/\/\/
                    else if(labelCheck=='L'){
                        instructiontoissueeltanya.setTimer(loadTimer);
                        instructiontoissueeltanya.StartExec = true;
                        instructiontoissueeltanya.startCycle= cyclenumber;
                        updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                        awzeenwriteback.add(instructiontoissueeltanya);
                    }
                    else if(labelCheck=='S'){
                    	//CASE1: LESA MA3MOLAHA ISSUE TABEE3Y HATDAWAR 3ALA ITS OPERANDS FEL REG FILE 
                    	//CASE2: GRABBED THE OPERANDS FROM THE BUS SO WILL START EXECUTION NEXT CYCLE (DON'T LOOK AT REG FILE)
                    	
                    	//CASE1: 
                    	if(instructiontoissueeltanya.Qj.equals("")) {

                    		//LOGICALLY THE STORE HAS SRC AND TARGET (STORE DOES NOT HAVE A DEST) BUT HERE THE TARGET IS THE ADDRESS
                    		//AND IT IS NOT CALCULATED OR TAKEN FROM ANY WHERE SO IT IS ALWAYS READY SO WE ONLY CARE ABOUT 
                    		//THE SRC OPERAND (QJ,VJ)
                    		//EXTRACT SRC FROM THE INSTRUCTION 
                    		//GO TO INT REG FILE WITH THE SRC
                    		//CHECK ON FILE[SRC].QI = 0 ?
                    		//IF YES THEN SET THE QJ = 0 AND VJ = VALUE 
                    		//IF NOT THEN SET THE QJ = QI AND THAT IS IT 
                    		int src = Integer.parseInt(instructiontoissueeltanya.src);
                    		
                    		if(intRegfile.registers[src].Qi.equals("0")) {
                    			instructiontoissueeltanya.Qj= "0";
                    			instructiontoissueeltanya.Vj= (double)intRegfile.registers[src].valueR;
                    		}
                    		else {
                    			instructiontoissueeltanya.Qj = intRegfile.registers[src].Qi;
                    		}
                    		
                    	}
                    	//NOW CHECK ON QJ IF = 0 THEN: 1- SET TIMER 2- START EXEC 3- ADD TO AWZEEN
                    	if(instructiontoissueeltanya.Qj.equals("0")) {
                    		instructiontoissueeltanya.setTimer(storeTimer);
                    		instructiontoissueeltanya.StartExec = true;
                            instructiontoissueeltanya.startCycle= cyclenumber;
                            updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                    		awzeenwriteback.add(instructiontoissueeltanya);
                    	}
                    	
                    	
                    }
                    // LOAD/ STORE ^^^^^^^^^^^^^^^^^^
                }
                else if(instructiontoissueeltanya.type == Type.F) //look at the int reg file
                {   //IF ARITHMETIC OPERATION\/\/\/\/\/\/\/\/\/
                     if (labelCheck=='A' || labelCheck=='M'){
                    	//CASE1: LESA MA3MOLAHA ISSUE TABEE3Y HATDAWAR 3ALA ITS OPERANDS FEL REG FILE 
                     	//CASE2: GRABBED THE OPERANDS FROM THE BUS SO WILL START EXECUTION NEXT CYCLE (DON'T LOOK AT REG FILE)
                     	
                     	//CASE1: 
                     	if(instructiontoissueeltanya.Qj.equals("") && instructiontoissueeltanya.Qk.equals("")) {
                     		//IF ARITHMETIC WE NEED BOTH (VJ, VK) AND (QJ, QK)
                     		//1- EXTRACT SRC, TARGET, DEST
                     		int src = Integer.parseInt(instructiontoissueeltanya.src);
                     		int target = Integer.parseInt(instructiontoissueeltanya.target);
                     		int destination = Integer.parseInt(instructiontoissueeltanya.destination);
                     		
                     		//2- GO TO FLOAT REG FILE WITH SRC 
                     		//3- CHECK ON FILE[SRC].QI 
                     		//4- IF 0 THEN SET QJ = 0 AND VJ = VALUE
                     		//5- BUT IF NOT? SET QJ = QI AND THAT IS IT
                     		if(floatRegfile.registers[src].Qi.equals("0")) {
                     			instructiontoissueeltanya.Qj = "0";
                     			instructiontoissueeltanya.Vj = floatRegfile.registers[src].valueF;
                     		}
                     		else {
                     			instructiontoissueeltanya.Qj = floatRegfile.registers[src].Qi;
                     		}
                     		
                     		//6- GO TO FLOAT REG FILE WITH TARGET 
                     		//7- CHECK ON FILE[TARGET].QI 
                     		//8- IF 0 THEN SET QK = 0 AND VK = VALUE
                     		//9- BUT IF NOT? SET QK = QI AND THAT IS IT
                            if (!instructiontoissueeltanya.operation.equals("ADDI") && !instructiontoissueeltanya.operation.equals("SUBI")){
                                if(floatRegfile.registers[target].Qi.equals("0")) {
                                    instructiontoissueeltanya.Qk = "0";
                                    instructiontoissueeltanya.Vk = floatRegfile.registers[target].valueF;
                                }
                                else {
                                    instructiontoissueeltanya.Qk = floatRegfile.registers[target].Qi;
                                }
                            }
                            else{
                                instructiontoissueeltanya.Qk = "0";
                                    instructiontoissueeltanya.Vk = Double.parseDouble(instructiontoissueeltanya.target) ;
                            }
                     		
                     		//AFTER WE TOOK OUR OPERANDS FROM THE REGISTER FILE WE THEN PUT OUR LABEL
                     		floatRegfile.registers[destination].Qi = instructiontoissueeltanya.label;
                     		
                     	}
                 		//FINALLY WE CHECK IF THE TWO OPERANDS ARE READY 
                 		//IF YES THEN: 1- SET TIMER  2- SET STARTEXEC  3- ADD IT TO AWZEEN 
                 		if(instructiontoissueeltanya.Qj.equals("0") && instructiontoissueeltanya.Qk.equals("0")) {
                            if(labelCheck == 'A'){
                                if(instructiontoissueeltanya.operation.equals("SUB.D") || instructiontoissueeltanya.operation.equals("DSUB")){
                                    instructiontoissueeltanya.setTimer(subTimer);
                                }
                                else if(instructiontoissueeltanya.operation.equals("ADD.D") || instructiontoissueeltanya.operation.equals("DADD")){
                                    instructiontoissueeltanya.setTimer(addTimer);
                                    
                                }
                                else if(instructiontoissueeltanya.operation.equals("ADDI") ){
                                    instructiontoissueeltanya.setTimer(addI);
                                }
                            }
                            else if(labelCheck == 'M'){
                                if(instructiontoissueeltanya.operation.equals("MUL.D") || instructiontoissueeltanya.operation.equals("DMUL")){
                                    instructiontoissueeltanya.setTimer(mulTimer);
                                }
                                else if(instructiontoissueeltanya.operation.equals("DIV.D") || instructiontoissueeltanya.operation.equals("DDIV")){
                                     instructiontoissueeltanya.setTimer(divTimer);
                                }
                            }
                 			instructiontoissueeltanya.StartExec = true;
                            instructiontoissueeltanya.startCycle= cyclenumber;
                            updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                 			awzeenwriteback.add(instructiontoissueeltanya);
                 		}
                     }
                     
                     else if(labelCheck == 'B') {
                    	//CASE1: LESA MA3MOLAHA ISSUE TABEE3Y HATDAWAR 3ALA ITS OPERANDS FEL REG FILE 
                     	//CASE2: GRABBED THE OPERANDS FROM THE BUS SO WILL START EXECUTION NEXT CYCLE (DON'T LOOK AT REG FILE)
                     	
                     	//CASE1: 
                     	if(instructiontoissueeltanya.Qj.equals("")) {
                     		//IF BRANCH WE ONLY NEED THE SRC REGISTER (VJ AND QJ)
                     		//EXTRACT THE SRC REG ONLY (AS BRANCH DOES NOT EVEN HAVE A DEST AND A TARGET)
                     		//GO TO FLOAT REG FILE WITH THE SRC
                     		//CHECK ON FILE[SRC].QI = 0 ?
                     		//IF YES THEN SET THE QJ = 0 AND VJ WITH THE VALUE 
                     		//IF NOT THEN SET THE QJ WITH QI AND THAT IS IT 
                     		int src = Integer.parseInt(instructiontoissueeltanya.src);
                     		if(floatRegfile.registers[src].Qi.equals("0")) {
                     			instructiontoissueeltanya.Qj = "0";
                     			instructiontoissueeltanya.Vj = floatRegfile.registers[src].valueF;
                     		}
                     		else {
                     			instructiontoissueeltanya.Qj = floatRegfile.registers[src].Qi;
                     		}
                     	}
                     	//THEN CHECK ON QJ IF IT IS 0 NOW
                 		//IF YES THEN : 1- SET TIMER  2- SET START EXEC  3- ADD IT TO AWZEEN (THE BRANCH WILL NOT REALLY WRITE BACK 
                 		//WE WILL JUST CHECK THEN IF THE VALUE "TRUE" MEANS TAKEN "FALSE" MEANS NOT TAKEN
                 		if(instructiontoissueeltanya.Qj.equals("0")) {
                 			instructiontoissueeltanya.setTimer(branchTimer);
                 			instructiontoissueeltanya.StartExec = true;
                            instructiontoissueeltanya.startCycle= cyclenumber;
                            updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                 			awzeenwriteback.add(instructiontoissueeltanya);
                 		}
                     	
                     }
                     //ARITMETIC ^^^^^^^^^^^^^^^^^^^^^
                    
                     //if LOAD/ STORE  INSTRUCTIONS \/\/\/\/\/\/\/\/\/\/
                    else if(labelCheck=='L'){
                        int destination = Integer.parseInt(instructiontoissueeltanya.destination);
                        floatRegfile.registers[destination].Qi = instructiontoissueeltanya.label;
                        instructiontoissueeltanya.setTimer(loadTimer);
                        instructiontoissueeltanya.StartExec = true;
                        instructiontoissueeltanya.startCycle= cyclenumber;
                        updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                        awzeenwriteback.add(instructiontoissueeltanya);

                    }
                    else if(labelCheck=='S'){
                    	//CASE1: LESA MA3MOLAHA ISSUE TABEE3Y HATDAWAR 3ALA ITS OPERANDS FEL REG FILE 
                    	//CASE2: GRABBED THE OPERANDS FROM THE BUS SO WILL START EXECUTION NEXT CYCLE (DON'T LOOK AT REG FILE)
                    	
                    	//CASE1: 
                    	if(instructiontoissueeltanya.Qj.equals("")) {
                    		//IF IT IS A STORE WE OLY NEED TO CHECK ON ONE OPERAND (QJ, VJ)
                    		//EXTRACT THE SRC 
                    		//GO TO THE FLOAT REGISTER FILE[SRC].QI 
                    		//CHECK IF IT IS = 0 ?
                    		//IF YES THEN SET QJ = 0 AND VJ = VALUE
                    		//IF NO THEN SET THE QJ TO QI AND THAT IS IT 
                    		int src = Integer.parseInt(instructiontoissueeltanya.src);
                    		
                    		if(floatRegfile.registers[src].Qi.equals("0")) {
                    			instructiontoissueeltanya.Qj= "0";
                    			instructiontoissueeltanya.Vj= floatRegfile.registers[src].valueF;
                    		}
                    		else {
                    			instructiontoissueeltanya.Qj = floatRegfile.registers[src].Qi;
                    		}
                    	}
                    	//NOW CHECK ON QJ IF = 0 THEN: 1- SET TIMER 2- START EXEC 3- ADD TO AWZEEN
                		if(instructiontoissueeltanya.Qj.equals("0")) {
                			instructiontoissueeltanya.setTimer(storeTimer);
                			instructiontoissueeltanya.StartExec = true;
                            instructiontoissueeltanya.startCycle= cyclenumber;
                            updateInstructionTable(instructiontoissueeltanya, "start", cyclenumber);
                			awzeenwriteback.add(instructiontoissueeltanya);
                		}
                    }
                    // LOAD/ STORE ^^^^^^^^^^^^^^^^^^

                }

    }
    public void execute(){
        //Loop on all the reservation stations 
        for(Instruction instruction: addsubReservation.reservationStation){
            if(instruction!=null){
            	if(!instruction.Exit) {
            		if(!instruction.WriteBack){
            			if (instruction.StartExec == false){
            				checker(instruction);
            			}
            			else{
            				
            				if(instruction.timer == 0){
            					instruction.StartExec = false;
            					calculateValue(instruction);
            					instruction.WriteBack = true;
                                instruction.endCycle= cyclenumber;
                                updateInstructionTable(instruction, "end", cyclenumber);
            					
            					
            				}
            				
            				instruction.timer --;
            			}
            			
            			
            		}
            		
            	}
             }
        }
       
        for(Instruction instruction: muldivReservation.reservationStation){
            if(instruction!=null){
            	if(!instruction.Exit) {
            		if(!instruction.WriteBack){
            			if (instruction.StartExec == false){
            				checker(instruction);
            			}
            			else{
            				//System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ timer: "+instruction.timer);
            				
            				
            				if(instruction.timer == 0){
                                instruction.endCycle= cyclenumber;
            					instruction.StartExec = false;
            					calculateValue(instruction);
            					instruction.WriteBack = true;
                                updateInstructionTable(instruction, "end", cyclenumber);
            					
            				}
            				
            				
            				instruction.timer --;
            			}
            			
            		}
            		
            	}
            }
        }

        for(Instruction instruction: loadReservation.reservationStation){
            if(instruction!=null){
            	if(!instruction.Exit) {
            		if(!instruction.WriteBack){
            			if (instruction.StartExec == false){
            				checker(instruction);
            			}
            			else{
            				
            				
            				
            				if(instruction.timer == 0){
                                instruction.endCycle= cyclenumber;
            					instruction.StartExec = false;
            					calculateValue(instruction);
            					instruction.WriteBack = true;
                                updateInstructionTable(instruction, "end", cyclenumber);
            					
            				}
            				
            				instruction.timer --;
            				
            				
            			}
            		}
            		
            	}
            }
        }

        for(Instruction instruction: storeReservation.reservationStation){
            if(instruction!=null){
            	if(!instruction.Exit) {
            		if(!instruction.WriteBack){
            			if (instruction.StartExec == false){
            				checker(instruction);
            			}
            			
            			else{
            				if(instruction.timer == 0){
                                instruction.endCycle= cyclenumber;
            					instruction.StartExec = false;
            					calculateValue(instruction);
            					instruction.WriteBack = true;
                                updateInstructionTable(instruction, "end", cyclenumber);
            					
            				}
            				
            				
            				instruction.timer --;
            			}
            			
            			
            			
            		}
            		
            	}
        }    
    }
}
    public void calculateValue(Instruction instruction){
        switch (instruction.operation) {
            case "ADD.D":
                instruction.value = instruction.Vj + instruction.Vk;
                //System.out.println("ADD VALUEEEEE: " + instruction.Vj + " + " + instruction.Vk + " = " + instruction.value);
                break;
            case "SUB.D":
                instruction.value = instruction.Vj - instruction.Vk;
                //System.out.println("SUB VALUEEEEE: " + instruction.Vj + " - " + instruction.Vk + " = " + instruction.value);
                break;
            case "DADD":
                instruction.value = instruction.Vj + instruction.Vk;
                //System.out.println("ADD VALUEEEEE: " + instruction.Vj + " + " + instruction.Vk + " = " + instruction.value);
                break;
            case "DSUB":
                instruction.value = instruction.Vj - instruction.Vk;
                //System.out.println("SUB VALUEEEEE: " + instruction.Vj + " - " + instruction.Vk + " = " + instruction.value);
                break;
            case "MUL.D":
                instruction.value = instruction.Vj * instruction.Vk;
                //System.out.println("MUL VALUEEEEE: " + instruction.Vj + " * " + instruction.Vk + " = " + instruction.value);
                break;
            case "DIV.D":
                instruction.value = instruction.Vj / instruction.Vk;
                break;
            case "DMUL":
                instruction.value = instruction.Vj * instruction.Vk;
                //System.out.println("MUL VALUEEEEE: " + instruction.Vj + " * " + instruction.Vk + " = " + instruction.value);
                break;
            case "DDIV":
                instruction.value = instruction.Vj / instruction.Vk;
                break;
            case "L.D":
                int address = Integer.parseInt(instruction.target) ;
                if(instruction.type == Type.R){
                    instruction.value = (double) intCache.values[address];
                }
                else if (instruction.type == Type.F){
                    //System.out.println("AYY HAGAAAAAAAA");
                    instruction.value = floatCache.values[address];
                }
                break;
            case "S.D": 
            	instruction.value = instruction.Vj;
            	break;
            case "BNEZ":
            instruction.value = instruction.Vj - 0;
            break;
            case "BNE":
                instruction.value = instruction.Vj - instruction.Vk; // Compare register1 (Vj) and register2 (Vk)
                break;
            case "BEQ":
                instruction.value = instruction.Vj - instruction.Vk; // Compare register1 (Vj) and register2 (Vk)
                break;


            case "ADDI":
                instruction.value = instruction.Vj + Integer.parseInt(instruction.target);
                break;
            case "SUBI":
                instruction.value = instruction.Vj - Integer.parseInt(instruction.target);
                break;
            default:
                break;
        }
    } 
    public void writeBack(){

        if (!awzeenwriteback.isEmpty()){
        Instruction writebackInstruction = awzeenwriteback.peek();
            if (writebackInstruction.WriteBack == true){
                awzeenwriteback.poll();
                double wbvalue = writebackInstruction.value;
                String wblabel = writebackInstruction.label;
                
                if(writebackInstruction.operation.equals("S.D")) {
                	//IF THE INSTRUCTION IS A STORE SO NO ONE IS NEEDING IT WE WILL NOT LOOP ON RES STATIONS OR REG FILES
                	//INSTEAD WE WILL GO TO THIS ADDRESS IN THE CACHE (INT OR FLOAT) AND STORE THE VALUE
                	int address = Integer.parseInt(writebackInstruction.target);
                	if(writebackInstruction.type == Type.R) {
                		intCache.values[address] = (int)((double)writebackInstruction.value);
                	}
                	else if(writebackInstruction.type == Type.F) {
                		floatCache.values[address] = writebackInstruction.value;
                	}
                }
                else {
					
                	for(Instruction instruction: addsubReservation.reservationStation){
                		if(instruction!=null){
                			if (instruction.Qj == wblabel){
                				instruction.Qj = "0";
                				instruction.Vj = wbvalue;
                				//CALL CHECKER ON THIS INSTRUCTION 
                				//IT WILL TRIGER THE CASE THAT QJ IS ALREADY 0 AND THE VALUE IS READY SO IT WILL NOT GO GET IT FROM REG FILE
                				//AND CHECKER WILL DO THE 3 THINGS 1- SET THE TIMER 2- SET START EXEC 3- ADD TO AWZEEM
                				//WHICH IS CRUCIAL TO DO ONCE WE RECIEVED THE VALUE IN THIS SAME CYCLE
                				checker(instruction);
                			}
                			if (instruction.Qk == wblabel){
                				instruction.Qk = "0";
                				instruction.Vk = wbvalue;
                				//CALL CHECKER ON THIS INSTRUCTION 
                				//IT WILL TRIGER THE CASE THAT QK IS ALREADY 0 AND THE VALUE IS READY SO IT WILL NOT GO GET IT FROM REG FILE
                				//AND CHECKER WILL DO THE 3 THINGS 1- SET THE TIMER 2- SET START EXEC 3- ADD TO AWZEEM
                				//WHICH IS CRUCIAL TO DO ONCE WE RECIEVED THE VALUE IN THIS SAME CYCLE
                				checker(instruction);
                			}
                		}
                	}
                	
                	for(Instruction instruction: muldivReservation.reservationStation){
                		if(instruction!=null){
                			if (instruction.Qj == wblabel){
                				instruction.Qj = "0";
                				instruction.Vj = wbvalue;
                				//CALL CHECKER ON THIS INSTRUCTION 
                				//IT WILL TRIGER THE CASE THAT QJ IS ALREADY 0 AND THE VALUE IS READY SO IT WILL NOT GO GET IT FROM REG FILE
                				//AND CHECKER WILL DO THE 3 THINGS 1- SET THE TIMER 2- SET START EXEC 3- ADD TO AWZEEM
                				//WHICH IS CRUCIAL TO DO ONCE WE RECIEVED THE VALUE IN THIS SAME CYCLE
                				checker(instruction);
                			}
                			
                			if (instruction.Qk == wblabel){
                				instruction.Qk = "0";
                				instruction.Vk = wbvalue;
                				//CALL CHECKER ON THIS INSTRUCTION 
                				//IT WILL TRIGER THE CASE THAT QK IS ALREADY 0 AND THE VALUE IS READY SO IT WILL NOT GO GET IT FROM REG FILE
                				//AND CHECKER WILL DO THE 3 THINGS 1- SET THE TIMER 2- SET START EXEC 3- ADD TO AWZEEM
                				//WHICH IS CRUCIAL TO DO ONCE WE RECIEVED THE VALUE IN THIS SAME CYCLE
                				checker(instruction);
                			}
                		}
                	}
                	
                	
                	// I GUESS THE LOAD IS NOT WAITING FOR ANYONE TO GIVE IT VALUE (WE DON'T EVEN CONSIDER IT HAS QJ/QK)
//                	
                	
                	for(Instruction instruction: storeReservation.reservationStation){
                		if(instruction!=null){
                			//THE STORE ONLY HAS QJ
                			if (instruction.Qj == wblabel){
                				instruction.Qj = "0";
                				instruction.Vj = wbvalue;
                				//CALL CHECKER ON THIS INSTRUCTION 
                				//IT WILL TRIGER THE CASE THAT QJ IS ALREADY 0 AND THE VALUE IS READY SO IT WILL NOT GO GET IT FROM REG FILE
                				//AND CHECKER WILL DO THE 3 THINGS 1- SET THE TIMER 2- SET START EXEC 3- ADD TO AWZEEM
                				//WHICH IS CRUCIAL TO DO ONCE WE RECIEVED THE VALUE IN THIS SAME CYCLE
                				checker(instruction);
                			}
                		}
                	}
                	if (writebackInstruction.type == Type.R){
                		if (writebackInstruction.operation == "BNEZ"){
                			for (int i = 0; i<labellist.size();i++){
                				Label currentlabel = labellist.get(i);
                				if (currentlabel.labelname == writebackInstruction.branchlabel){
                					index = currentlabel.index;
                				}
                			}
                            stopIssue = false;
                		}
                		else{
                			//WRITING VALUE TO INT REGISTER FILE
                			for (int i = 0; i<intRegfile.registers.length; i++){
                				if (intRegfile.registers[i].Qi == wblabel){
                					intRegfile.registers[i].Qi = "0";
                					intRegfile.registers[i].valueR = (int)wbvalue;
                				}
                			}
                		}
                		
                	}
                	else if (writebackInstruction.type == Type.F){
                		if (writebackInstruction.operation == "BNEZ"){
                			if (writebackInstruction.value != 0){
                				for (int i = 0; i<labellist.size();i++){
                					Label currentlabel = labellist.get(i);
                					System.out.println("CURRENT Label name: " + currentlabel.labelname);
                					System.out.println("BRANCH LABEL name: " + writebackInstruction.branchlabel);
                					if (currentlabel.labelname.equals( writebackInstruction.branchlabel)){
                						index = currentlabel.index;
                						System.out.println("NEW INDEXXXXXXXX: " + index);
                					}
                				}
                			}
                            stopIssue = false;
                		}
                		else{
                			//WRITING VALUE TO FLOAT REGISTER FILE
                			for (int i = 0; i < floatRegfile.registers.length; i++){
                				if (floatRegfile.registers[i].Qi == wblabel){
                					floatRegfile.registers[i].Qi = "0";
                					floatRegfile.registers[i].valueF = wbvalue;
                				}
                			}
                		}
                	}
                	
                	if (writebackInstruction.type == Type.R) {
                	    if (writebackInstruction.operation.equals("BNE")) {
                	        if (writebackInstruction.value != 0) { // BNE condition: R1 != R2
                	            for (int i = 0; i < labellist.size(); i++) {
                	                Label currentlabel = labellist.get(i);
                	                if (currentlabel.labelname.equals(writebackInstruction.branchlabel)) {
                	                    index = currentlabel.index; // Update to branch index
                	                    break;
                	                }
                	            }
                	        }
                	        stopIssue = false; // Allow issuing further instructions
                	    } else {
                	        // WRITING VALUE TO INT REGISTER FILE
                	        for (int i = 0; i < intRegfile.registers.length; i++) {
                	            if (intRegfile.registers[i].Qi.equals(wblabel)) {
                	                intRegfile.registers[i].Qi = "0";
                	                intRegfile.registers[i].valueR = (int) wbvalue;
                	            }
                	        }
                	    }
                	} else if (writebackInstruction.type == Type.F) {
                	    if (writebackInstruction.operation.equals("BNE")) {
                	        if (writebackInstruction.value != 0) { // BNE condition: R1 != R2
                	            for (int i = 0; i < labellist.size(); i++) {
                	                Label currentlabel = labellist.get(i);
                	                if (currentlabel.labelname.equals(writebackInstruction.branchlabel)) {
                	                    index = currentlabel.index; // Update to branch index
                	                    break;
                	                }
                	            }
                	        }
                	        stopIssue = false; // Allow issuing further instructions
                	    } else {
                	        // WRITING VALUE TO FLOAT REGISTER FILE
                	        for (int i = 0; i < floatRegfile.registers.length; i++) {
                	            if (floatRegfile.registers[i].Qi.equals(wblabel)) {
                	                floatRegfile.registers[i].Qi = "0";
                	                floatRegfile.registers[i].valueF = wbvalue;
                	            }
                	        }
                	    }
                	}
                	
                	//
                	if (writebackInstruction.type == Type.R) {
                	    if (writebackInstruction.operation.equals("BEQ")) {
                	        if (writebackInstruction.value == 0) { // BNE condition: R1 != R2
                	            for (int i = 0; i < labellist.size(); i++) {
                	                Label currentlabel = labellist.get(i);
                	                if (currentlabel.labelname.equals(writebackInstruction.branchlabel)) {
                	                    index = currentlabel.index; // Update to branch index
                	                    break;
                	                }
                	            }
                	        }
                	        stopIssue = false; // Allow issuing further instructions
                	    } else {
                	        // WRITING VALUE TO INT REGISTER FILE
                	        for (int i = 0; i < intRegfile.registers.length; i++) {
                	            if (intRegfile.registers[i].Qi.equals(wblabel)) {
                	                intRegfile.registers[i].Qi = "0";
                	                intRegfile.registers[i].valueR = (int) wbvalue;
                	            }
                	        }
                	    }
                	} else if (writebackInstruction.type == Type.F) {
                	    if (writebackInstruction.operation.equals("BEQ")) {
                	        if (writebackInstruction.value == 0) { // BNE condition: R1 != R2
                	            for (int i = 0; i < labellist.size(); i++) {
                	                Label currentlabel = labellist.get(i);
                	                if (currentlabel.labelname.equals(writebackInstruction.branchlabel)) {
                	                    index = currentlabel.index; // Update to branch index
                	                    break;
                	                }
                	            }
                	        }
                	        stopIssue = false; // Allow issuing further instructions
                	    } else {
                	        // WRITING VALUE TO FLOAT REGISTER FILE
                	        for (int i = 0; i < floatRegfile.registers.length; i++) {
                	            if (floatRegfile.registers[i].Qi.equals(wblabel)) {
                	                floatRegfile.registers[i].Qi = "0";
                	                floatRegfile.registers[i].valueF = wbvalue;
                	            }
                	        }
                	    }
                	}
                	
                	
				}
        //writebackInstruction.setVj(0);
        //writebackInstruction.setVk(0);
        writebackInstruction.setBusy(0);
        //writebackInstruction.setTarget("0");
        //writebackInstruction.setDependencyFlag(false);
        //writebackInstruction.operation = "N/A";
        writebackInstruction.WriteBack = false;
        writebackInstruction.StartExec = false;
        writebackInstruction.Exit = true;
        writebackInstruction.writeCycle= cyclenumber;
        updateInstructionTable(writebackInstruction, "write", cyclenumber);
    }

}

}
    public void checkifdone(){
        boolean addsubdone = true;
        boolean muldivdone = true;
        boolean loaddone = true;
        boolean storedone = true;

        if (stillprinting == false){
            for(Instruction instruction: addsubReservation.reservationStation){
            if(instruction!=null){
               if(instruction.getBusy() == 1){
                   addsubdone = false;
               }
            }
        }
        
        for(Instruction instruction: muldivReservation.reservationStation){
            if(instruction!=null){
                if(instruction.getBusy() == 1){
                   muldivdone = false;
               }
            }
        }

        for(Instruction instruction: loadReservation.reservationStation){
            if(instruction!=null){
                if(instruction.getBusy() == 1){
                   loaddone = false;
               }
            }
        }

        for(Instruction instruction: storeReservation.reservationStation){
            if(instruction!=null){
                if(instruction.getBusy() == 1){
                   storedone = false;
                }
            }
        }
        if (addsubdone == true && muldivdone == true && loaddone == true && storedone == true){
            stillprintingbegad = false;
        }
        }



        for(Instruction instruction: addsubReservation.reservationStation){
            if(instruction!=null){
               if(instruction.getBusy() == 1){
                   addsubdone = false;
               }
            }
        }
        
        for(Instruction instruction: muldivReservation.reservationStation){
            if(instruction!=null){
                if(instruction.getBusy() == 1){
                   muldivdone = false;
               }
            }
        }

        for(Instruction instruction: loadReservation.reservationStation){
            if(instruction!=null){
                if(instruction.getBusy() == 1){
                   loaddone = false;
               }
            }
        }

        for(Instruction instruction: storeReservation.reservationStation){
            if(instruction!=null){
                if(instruction.getBusy() == 1){
                   storedone = false;
                }
            }
        }
        if (addsubdone == true && muldivdone == true && loaddone == true && storedone == true){
            stillprinting = false;
        }
    }
    
    public static void main(String[] args) throws Exception {
        App app = new App();
        intRegfile = new FileReg();
        intRegfile.setType(Type.R);
        floatRegfile= new FileReg();
        floatRegfile.setType(Type.F);
        intCache = new IntCache(20);
        floatCache = new FloatCache(20);

        app.eshtaghal();
        
    }
}
