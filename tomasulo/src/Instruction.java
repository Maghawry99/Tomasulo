import java.util.Objects;

public class Instruction{
    String operation;
    String destination;
    String src;
    String target;
    int busy; // 0 = not busy, 1 = busy
    Double Vj, Vk;
    String Qj, Qk;
    String label= " "; //ADD, SUB, MUL, DIV, LOAD, STORE
    Type type;
    int timer; 
    Double value;
    boolean StartExec = false;
    boolean WriteBack = false;
    boolean executeme= false;
    boolean dependencyflag = false;
    boolean Exit = false;
    String branchlabel;
    int issueCycle;
    int startCycle;
    int endCycle;
    int writeCycle;
    private static int nextId = 0; // Static counter for generating unique IDs
    private int id;
    String register1;
    String register2;

    
    //LOAD INSTRICTION
    public Instruction(char type, String operation, String register, String address){
        this.operation = operation;
        if(operation.equals("L.D")){
            this.destination= register;
        }
        else if(operation.equals("S.D")){
            this.src = register;
        }
        this.target=address;
        if (type == 'R'){
            this.type = Type.R;
        }
        else if (type == 'F'){
            this.type = Type.F;
        }
        else{
            System.out.println("ERROR: Instruction type not recognized");
        }
        this.value = 0.0;
        this.busy  = 0;
        this.Qj = "";
        this.Qk = "";
    }

    //STORE INSTRUCTION
    // public Instruction(char type, String destination, String address, String value){
    //     this.destination= destination;
    //     this.address=address;
    //     if (type == 'R'){
    //         this.type = Type.R;
    //     }
    //     else if (type == 'F'){
    //         this.type = Type.F;
    //     }
    //     else{
    //         System.out.println("ERROR: Instruction type not recognized");
    //     }
    //     this.value= value;
    // }

    //ARITHMETIC INSTRUCTION
    public Instruction(char type, String operation, String destination, String src, String target){
      
        this.operation = operation;
        this.destination = destination;
        this.src = src;
        this.target = target;
        if (type == 'R'){
            this.type = Type.R;
        }
        else if (type == 'F'){
            this.type = Type.F;
        }
        else{
            System.out.println("ERROR: Instruction type not recognized");
        }
        Vj = 0.0;
        Vk = 0.0;
        Qj = "";
        Qk = "";


    }

    //BNEZ
    public Instruction(String operation, char type, String src, String label, String branchlabel){

        if (type == 'R'){
            this.type = Type.R;
        }
        else if (type == 'F'){
            this.type = Type.F;
        }
        else{
            System.out.println("ERROR: Instruction type not recognized");
        }
        this.operation = operation;
        this.src = src;
        this.label = label;
        this.branchlabel = branchlabel;
        //Qk , target and destination should be "" but i am postponing this
        this.Qk = "0";
        this.Vk = 0.0;
        this.Qj = "";
        this.Vj = 0.0;
        this.target = "0";
        this.destination = "0";

    }
    
   

    
    //BNE BEQ
    public Instruction(String operation, char type, String label, String register1, String register2, String branchLabel) {
        // Check and set the instruction type
        if (type == 'R') {
            this.type = Type.R;
        } else if (type == 'F') {
            this.type = Type.F;
        } else {
            System.out.println("ERROR: Instruction type not recognized");
        }

        // Assign operation, label, and branch details
        this.operation = operation;
        this.label = label; // Instruction label
        this.branchlabel = branchLabel; // Branch target (e.g., LOOP)

        // Registers for comparison
        this.register1 = register1; // R1
        this.register2 = register2; // R2

        // Initialize default values for additional attributes
        this.Qk = "0";
        this.Vk = 0.0;
        this.Qj = "0";
        this.Vj = 0.0;
        this.target = "0";
        this.destination = "0";

      
    }
   


    
    


    public Instruction() {
    }
    
    public String getOperation(){
        return operation;
    }
    public String getLabel(){
        return label;
    }
    public int getBusy(){
        return busy;
    }
    public void setBusy(int newbusy){
        this.busy = newbusy;
    }
    public void setLabel(String newlabel){
        this.label = newlabel;
    }
    public void setTimer(int timer){
        this.timer = timer;
    }
    public void setVj(double newVj){
        this.Vj = newVj;
    }
    public void setVk(double newVk){
        this.Vk = newVk;
    }
    public void setTarget(String newTarget){
        this.target = newTarget;
    }
    public void setDependencyFlag (boolean newFlag){
        this.dependencyflag = newFlag;
    }
    public int getTimer(){
        return timer;
    }

    @Override
public boolean equals(Object obj) {
    // Check if the object is compared with itself
    if (this == obj) {
        return true;
    }

    // Check if obj is an instance of Instruction or not
    if (!(obj instanceof Instruction)) {
        return false;
    }

    // Typecast obj to Instruction so that we can compare data members
    Instruction other = (Instruction) obj;

     switch(operation){
            case "ADD.D": 
            case "DADD":
            case "SUB.D":
            case "DSUB":
            case "MUL.D":
            case "DMUL":
            case "DIV.D":
            case "DDIV":
            case "ADDI":
            case "SUBI":
                return Objects.equals(this.operation, other.operation)
           && Objects.equals(this.destination, other.destination)
           && Objects.equals(this.src, other.src)
           && Objects.equals(this.target, other.target)
           && Objects.equals(this.issueCycle, other.issueCycle);

            case "L.D":
                 return Objects.equals(this.operation, other.operation)
           && Objects.equals(this.destination, other.destination)
           && Objects.equals(this.target, other.target)
            && Objects.equals(this.issueCycle, other.issueCycle);
            case "S.D":
               return Objects.equals(this.operation, other.operation)
           && Objects.equals(this.src, other.src)
           && Objects.equals(this.target, other.target)
            && Objects.equals(this.issueCycle, other.issueCycle);
            case "BNEZ": 
                return Objects.equals(this.operation, other.operation)
           && Objects.equals(this.src, other.src)
           && Objects.equals(this.branchlabel, other.branchlabel)
            && Objects.equals(this.issueCycle, other.issueCycle);
            case "BNE":
                return Objects.equals(this.operation, other.operation)
                    && Objects.equals(this.register1, other.register1)
                    && Objects.equals(this.register2, other.register2)
                    && Objects.equals(this.branchlabel, other.branchlabel)
                    && Objects.equals(this.issueCycle, other.issueCycle);

            default: return false;


          }
          
}

    public String getInstruction(){
          String printThis = "";
          switch(operation){
            case "ADD.D": 
            case "DADD":
            case "SUB.D":
            case "DSUB":
            case "MUL.D":
            case "DMUL":
            case "DIV.D":
            case "DDIV":
            case "ADDI":
            case "SUBI":
                printThis += operation + " " + type + destination + " " + type + src + " " + type + target;
            break;

            case "L.D":
                printThis += operation + " " + type + destination+ " " + target;
            break;

            case "S.D":
                printThis += operation + " " + type + src+ " " + target;
            break;

            case "BNEZ": 
                printThis += operation + " " + type + src+ " " + branchlabel;
            break;
            case "BNE": 
                printThis += operation + " " + type + " " + register1 + ", " + register2 + " " + branchlabel;
                break;


          }
          return printThis;
    }

    @Override
public String toString() {
    StringBuilder printed = new StringBuilder();
        String printThis;
        if (this == null || this.getLabel() == null) {
            printThis = "NULLLL AWYYYYY";
        } else {
            printThis = this.operation + " "+  this.type +" "+ this.destination + " "+ this.src + " "+this.target;
        }
        printed.append("Instruction is ").append(printThis).append("\n");

    return printed.toString();
}

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, Instruction!");
    }

}