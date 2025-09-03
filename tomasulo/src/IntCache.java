import java.util.Random;

public class IntCache {
    
    int[] values;
    int size;

    public IntCache(int size){
        this.size = size;
        values = new int[size];
        preLoad();
    }
    public void preLoad(){

        Random random = new Random();
        for(int i = 0; i < size; i++){
            int randomValue = random.nextInt(101); // Generates a random value between 0 and 100
            values[i]= randomValue;
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        System.out.println("------------------------");       

        System.out.printf("|%-10s| %-10s|\n",
                "Address", "Value");
        System.out.println("------------------------");   

        for (int i =0 ; i< size; i++) {
        System.out.printf("|%-10d| %-10d|\n",
                    i,values[i]);
        }
        System.out.println("------------------------");   

        return result.toString();
    }

    }
