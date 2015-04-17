import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class Sudoku {

	static int N = 9;
	static int quantAtrib=0;
	static int quantTests = 0;
	static int poda = 0;
	static int quantSoluc = 0;
	static int totalAtrib = 0;

	static int MatrizSudoku[][] = new int[9][9]; 

	static class CelLoc {

		int linha, coluna;

		public CelLoc(int linha, int coluna) {
			super();
			this.linha = linha;
			this.coluna = coluna;
		}

		@Override
		public String toString() {
			return "CelLoc [linha=" + linha + ", coluna=" + coluna + "]";
		}
	};

	//Função com o conjunto de restrições para preenchimento do valor na celula da matriz
	static boolean ValidarJogada(CelLoc cell, int valor) {


		// se celula ja estiver preenchida com qualquer outro valor (1 a 9)	
		if (MatrizSudoku[cell.linha][cell.coluna] != 0) {
			throw new RuntimeException(
					MatrizSudoku[cell.linha][cell.coluna] + "ja esta preenchido");
		}

		// caso na linha correspondente ja tenha o mesmo valor preenchido
		for (int c = 0; c < 9; c++) {
			if (MatrizSudoku[cell.linha][c] == valor)
				return false;
		}

		// caso na coluna correspondente ja tenha o mesmo valor preenchido
		for (int r = 0; r < 9; r++) {
			if (MatrizSudoku[r][cell.coluna] == valor)
				return false;
		}

		// caso o valor ja esteja presente na minha grade é retornado falso

		// validar box
		int x1 = 3 * (cell.linha / 3);
		int y1 = 3 * (cell.coluna / 3);
		int x2 = x1 + 2;
		int y2 = y1 + 2;

		for (int x = x1; x <= x2; x++)
			for (int y = y1; y <= y2; y++)
				if (MatrizSudoku[x][y] == valor)
					return false;

		//caso o valor não esteja presente na linha, coluna e no box então é retornado true
		return true;
	}

	//função para passar a proxima celula a ser analisada
	//essa função é chamada no caso da celula anterior ja estar preenchida
	static CelLoc getNextCell(CelLoc cur) {

		int linha = cur.linha;
		int coluna = cur.coluna;

		coluna++;

		//se coluna > 0 ja estamos no final da linha, então é passado para a proxima linha e a coluna passa a ser zero novamente.
		if (coluna > 8) {
			coluna = 0;
			linha++;
		}

		// caso a linha seja maior que 8 (9) então ja estamos no final da matriz, então retorna null
		if (linha > 8)
			return null; // reached end

		CelLoc next = new CelLoc(linha, coluna);
		return next;
	}

	// função backtracking
	static boolean backtracking(CelLoc cur) {


		// caso null, chegou-se ao fim então retorna true
		if (cur == null)
			return true;

		// caso minha celula ja tenha um valor, então passo para proxima celula e passo como parâmetro para chamada recursiva 
		// do backtracking
		if (MatrizSudoku[cur.linha][cur.coluna] != 0) {

			return backtracking(getNextCell(cur));
		}

		//A partir de agora sera verificado os possiveis valores (domonio) para serem atribuido a celula
		for (int i = 1; i <= 9; i++) {
			// validando o valor candidato a ser atribuido
			boolean valid = ValidarJogada(cur, i);

			//nessa condição irei receber o valor da poda escolhida para executar a função correspondente
			if ((valid && poda==1) || (valid && ok_fc(cur, i) && poda ==2)){ //backtracking simples ou com forward checking

				// atribuição do dominio a variavel
				MatrizSudoku[cur.linha][cur.coluna] = i;
				quantAtrib++;

				// repetir o processo com a proxima celula
				boolean solved = backtracking(getNextCell(cur));
				// caso tenha sido resolvido retorno true
				if (solved)
					return true;
				else
					MatrizSudoku[cur.linha][cur.coluna] = 0; //retornando o valor da variavel/celula para vazio
			}else
				continue;

		}

		//nenhum valor pode ser preenchido na celula, então é retornado false
		return false;


	}

	//função forward checking
	static boolean ok_fc(CelLoc cur, int value){

		//atribuição do valor a celula
		MatrizSudoku[cur.linha][cur.coluna] = value;


		//verificar por toda a matriz as celulas ainda vazias. Adicionando os valores (dominios) a uma lista.
		//quando não existir mais valores candidatos (lista de dominios null) então é retornando false e a celula volta a ser vazia
		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++)
				if(MatrizSudoku[x][y] == 0){
					ArrayList<Integer> candidatos = src_valores_candidatos(cur, x, y);
					if(candidatos.size()==0){
						MatrizSudoku[cur.linha][cur.coluna] = 0;
						return false;
					}
				}

		//caso contrario é retornado true
		MatrizSudoku[cur.linha][cur.coluna] = 0;
		return true;
	}

	//função de busca para o proximo valor/variavel pelo mrv
	static CelLoc busca_mrv_prox(CelLoc cur){
		int tamanho_dominio = 9; //tamanho maximo de dominios
		CelLoc next = null;

		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++)
				if(MatrizSudoku[x][y] == 0){ //verificar os valores candidatos à aquela localização
					ArrayList<Integer> candidatos = src_valores_candidatos(cur, x, y);
					if (candidatos.size() < tamanho_dominio ){
						tamanho_dominio = candidatos.size();
						next  = new CelLoc(x, y);
					}

				}

		return next;
	}

	//a busca mrv vai funcionar como o backtracking com a diferença que aqui 
	//irei pegar apenas os valores com menor quantidade de dominios validos

	static boolean busca_mrv(CelLoc cur) {

		// caso null, chegou-se ao fim então retorna true
		if (cur == null)
			return true;

		// caso minha celula ja tenha um valor, então passo para proxima celula,
		if (MatrizSudoku[cur.linha][cur.coluna] != 0) {

			return busca_mrv(getNextCell(cur));
		}

		//função similar ao backtracking
		for (int i = 1; i <= 9; i++) {
			// verificar e validar o valor
			boolean valid = ValidarJogada(cur, i);

			if (valid && ok_fc(cur, i) && poda ==3){ // chamada da validação com forward checking

				// atribuição do valor
				MatrizSudoku[cur.linha][cur.coluna] = i;
				quantAtrib++;

				// continuar processo com proxima celula
				boolean solved = busca_mrv(busca_mrv_prox(cur));
				// caso tenha sido resolvido retorno true
				if (solved)
					return true;
				else
					MatrizSudoku[cur.linha][cur.coluna] = 0;
				// continuar processo com outros valores
			}else
				continue;
		}

		//nenhum valor pode ser preenchido na celula, então é retornado false
		return false;


	}

	//função responsavel em procurar dominios candidatos e agrupa-los em um unico arraylist
	static ArrayList<Integer> src_valores_candidatos(CelLoc cur, int l, int c){

		ArrayList<Integer> dominios = new ArrayList<Integer> (); 

		boolean lista[] = new boolean[10];

		//calculo para box
		int x1 = 3 * (l / 3);
		int y1 = 3 * (c / 3);
		int x2 = x1 + 2;
		int y2 = y1 + 2;
		
		for(int x=0;x<9;x++)
			lista[MatrizSudoku[l][x]] = true;
		for(int y=0;y<9;y++)
			lista[MatrizSudoku[y][c]] = true;
		for (int x = x1; x <= x2; x++)
			for (int z = y1; z <= y2; z++)
				lista[MatrizSudoku[x][z]] = true;

		for(int k=1;k<=9;k++)
			if(lista[k]==false)
				dominios.add(k);

		return dominios;

	}

	//função para imprimir matriz solução
	static void printSudoku(int MatrizSudoku[][]) {
		for (int linha = 0; linha < N; linha++) {
			for (int coluna = 0; coluna < N; coluna++)
				System.out.print(MatrizSudoku[linha][coluna] + " ");
			System.out.println();
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		Scanner input = new Scanner(new File("entrada.txt"));
		boolean solved = false;
		int linha;
		int coluna;

		poda = 1; //backtracking
		//poda = 2; //backtracking + forward checking
		//poda = 3; //backtracking + forward checking + MVR

		quantTests = input.nextInt();

		for(int i=1;i<=quantTests;i++){

			//critério de parada 10^6, caso necessario
			/*if ( quantAtrib > Math.pow(10, 6)){
				System.out.println("Numero de atribuicoes excede limite maximo");
				System.out.println("");
				break;
			}*/

			for(linha=0;linha<9;linha++)
				for(coluna=0; coluna<9;coluna++)
					MatrizSudoku[linha][coluna] = input.nextInt();

			switch(poda){
			case 1:
				solved = backtracking(new CelLoc(0, 0));
			case 2: 
				solved = backtracking(new CelLoc(0, 0));
			case 3:
				solved = busca_mrv(busca_mrv_prox(new CelLoc(0, 0)));
			case 4:
				break;
			}

			if (!solved) {
				System.out.println("Não foi encontrada solução");
				//System.out.println("Quantidade atribuições: " +quantAtrib);
				return;
			}
			//System.out.println("Quantidade atribuições: " +quantAtrib);
			printSudoku(MatrizSudoku);
			System.out.println("");
			quantSoluc++;
			totalAtrib = +quantAtrib;
		}

		input.close();

		//dados quantitativos
		//System.out.println("Quantidade soluções: "+quantSoluc);
		//System.out.println("Quantidade atribuições: "+totalAtrib);
		//System.out.println("Aproveitamento de soluções: "+100*quantSoluc/95 + "%");


	}
}
