import java.util.Random;

public class FileReg {
    Register[] registers; 
    Type type;

    public FileReg(){
        registers = new Register[32];

        for(int i=0;i<32; i++){
            registers[i]= new Register(i);
        }
        preLoad();
    }

    public void setType(Type type){
        this.type = type;
    }

    public void preLoad(){
        Random random = new Random();

        for(int i = 0; i < 32; i++){
            int randomValue = random.nextInt(101); // Generates a random value between 0 and 100
            registers[i].valueR= randomValue;
        }
        //registers[2].valueR = 80;
        for(int i = 0; i < 32; i++){
            double randomValueF = random.nextDouble() * 100; // Generates a random floating-point value between 0 and 100
            registers[i].valueF = Math.floor(randomValueF * 100) / 100; // Truncate to two decimal places
        }
        registers[8].valueF = 2.0;
        registers[10].valueF = 1.0;
        registers[11].valueF = 3.0;
        registers[12].valueF = 2.0;
        registers [0].valueF = 0.0;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        System.out.println("-------------------------------------");       

        System.out.printf("|%-10s| %-10s | %-10s |\n",
                "Register","Qi", "Value");
        System.out.println("-------------------------------------");       
        for (Register register : registers) {
            if(type == Type.R){
                System.out.printf("|%-10d| %-10s | %-10d |\n",
                    register.number, register.Qi,register.valueR);
                
                
            }
            else{
                 System.out.printf("|%-10d| %-10s | %-10f |\n",
                   register.number, register.Qi,register.valueF);
                
            }
        }

        return result.toString();
    }

}