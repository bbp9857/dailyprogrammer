package com.gapid.permutations;

public class Permutation {
    static void IndexInvert(int J[], int N, int Idx)
    {  int M, K;

        for (M=1, K=N-1; K > 1; K--)   // Generate (N-1)!
            M *= K;
        for ( K = 0; M > 1; K++ )
        {  J[K] = Idx / M;   // Offset in dimension K
            Idx = Idx % M;    // Remove K contribution
            M /= --N;         // next lower factorial
        }
        J[K] = Idx;          // Right-most index
    }

    static void Permute (char Line[], char first, int n, int Jdx[])
    {  int limit;

        Line[0] = first;
        for (limit = 1; limit < n; limit++)
            Line[limit] = (char)(1+Line[limit-1]);

        for (limit = 0; limit < n; limit++)
        {  char Hold;
            int Idx = limit + Jdx[limit];
            Hold = Line[Idx];
            while (Idx > limit)
            {  Line[Idx] = Line[Idx-1];
                Idx--;
            }
            Line[Idx] = Hold;
        }
    }

    public static void main(String[] args) {
        char perm[]  = "abcde".toCharArray();
        int  Jdx[]   = new int[perm.length];
        int   Index = 33;

       IndexInvert(Jdx, perm.length, Index);
        Permute (perm, 'a', perm.length, Jdx);
        for (char c : perm){
            System.out.print(c + " ");
        }
        System.out.println();
    }
}
