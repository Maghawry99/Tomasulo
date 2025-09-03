import java.util.Random;

public class FloatCache{
    Double[] values; 
    int size;
    public FloatCache(int size){
        this.size = size;
        values = new Double[size];
        preLoad();
    }

     public void preLoad(){
        Random random = new Random();
        for(int i = 0; i < size; i++){
            double randomValueF = random.nextDouble() * 100; // Generates a random floating-point value between 0 and 100
            values[i] = Math.floor(randomValueF * 100) / 100; // Truncate to two decimal places
        }
        values[8] = 2.0;
        values[10] = 1.0;
        values[11] = 3.0;
        values[12] = 2.0;
        values [0] = 0.0;
    }
    public String toString() {
        StringBuilder result = new StringBuilder();

        System.out.println("----------------------------");       

        System.out.printf("|%-10s| %-15s|\n",
                "Address", "Value");
        System.out.println("----------------------------");   

        for (int i =0 ; i< size; i++) {
        System.out.printf("|%-10d| %-15f|\n",
                    i,values[i]);
        }
        System.out.println("----------------------------");   

        return result.toString();
    }


    
}