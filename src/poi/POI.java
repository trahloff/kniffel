package poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import helper.MapUtil;
import helper.SortedArrayList;

@SuppressWarnings({"unused" })
public class POI {

	private static File saveFile = new File("save.xls");
	private static Workbook getSave() throws IOException {
		FileInputStream input = new FileInputStream(saveFile);
		Workbook wb = new HSSFWorkbook(input);
		input.close();
		return wb;
	}
	private static Workbook getWorkbook() throws IOException {

		// much safer check than .exist(), because with .isFile() return==true ONLY when it exists & valid file
		if(saveFile.isFile()) {
			return getSave();
		}else {
			createSave();
			return getSave();
		}

	}
	private static void createSave() throws IOException {

		HSSFWorkbook workbook = new HSSFWorkbook();
		workbook.createSheet("placeholder").createRow(0);

		FileOutputStream output = new FileOutputStream(saveFile);
		workbook.write(output);

		output.close();
		workbook.close();

	}
	private static void createPlayer(String playerName) throws IOException {

		HSSFWorkbook workbook = (HSSFWorkbook) getWorkbook();

		try {
			workbook.createSheet(playerName);


		} catch (IllegalArgumentException e) { // Spieler schon vorhanden
			System.out.println("could not create player \""+playerName+"\"");
		}


		FileOutputStream output = new FileOutputStream(saveFile);
		workbook.write(output);

		output.close();
		workbook.close();

	}
	private static void deletePlayer(String playerName) throws IOException {

		HSSFWorkbook workbook = (HSSFWorkbook) getWorkbook();

		try {
			workbook.removeSheetAt(workbook.getSheetIndex(	workbook.getSheet(playerName)));
			System.out.println("deleted " + playerName);
		} catch (IllegalArgumentException e) {
			System.out.println("there is no player \""+playerName+"\" who could be deleted");
		}

		FileOutputStream output = new FileOutputStream(saveFile);
		workbook.write(output);

		output.close();
		workbook.close();

	}
	private static void saveScore(String player, Integer score) throws IOException {

		HSSFWorkbook workbook = (HSSFWorkbook) getWorkbook();

		HSSFSheet sheet = workbook.getSheet(player);

		if (sheet.getRow(0)==null) {
			sheet.createRow(0);
		}

		sheet.getRow(0).createCell(Math.abs(sheet.getRow(0).getLastCellNum()), Cell.CELL_TYPE_STRING).setCellValue(score);

		FileOutputStream output = new FileOutputStream(saveFile);
		workbook.write(output);

		output.close();
		workbook.close();


	}

	public static SortedArrayList<Integer> getScoreByPlayer(String player) {

		SortedArrayList<Integer> scores = new SortedArrayList<Integer>();
		try {

			Workbook wb = getWorkbook();

			try {

				Iterator<Row> rowIterator = wb.getSheetAt(wb.getSheetIndex(player)).iterator();
				while (rowIterator.hasNext()) {
					Iterator<Cell> cellIterator = rowIterator.next().cellIterator();
					while (cellIterator.hasNext()) {
						scores.insert((int) cellIterator.next().getNumericCellValue());
					}
				}

			} catch (IllegalArgumentException e) {
				System.err.println("No player with name: "+player+"\n"+e);
				return scores;
			}


		} catch (IOException e) {
			System.err.println(e);
			return scores; // man k�nnte hier auch sich code sparen und in einem finally block scores zur�ckgeben, ist aber bad practice. return geh�rt nicht in finally blocks
		}

		return scores;

	}
	public static Map<String, Integer>getAllScores() {

		Map<String, Integer> map = new TreeMap<String, Integer>();

		try {

			Iterator<Sheet> sheetIterator= getWorkbook().iterator();

			while (sheetIterator.hasNext()) {
				Sheet tmp = sheetIterator.next();
				Iterator<Row> rowIterator = tmp.iterator();
				while(rowIterator.hasNext()) {
					Iterator<Cell> cellIterator = rowIterator.next().cellIterator();
					while(cellIterator.hasNext()) {
						map.put(tmp.getSheetName(), (int) cellIterator.next().getNumericCellValue());
					}
				}
			}

		} catch (IOException e) {
			return map; // siehe +getScoreByPlayer(String)
		}

		return MapUtil.sortByValue(map);


	}
	public static ArrayList<String> getPlayerList() {

		ArrayList<String> players = new ArrayList<String>();

		try {

			Iterator<Sheet> sheetIterator= getWorkbook().iterator();

			while (sheetIterator.hasNext()) {
				players.add(sheetIterator.next().getSheetName());
			}


		} catch (Exception e) {
			System.out.println(e);
			JOptionPane.showMessageDialog(null, "The savefile \"save.xls\" can't be opened. Please close the file and try again.");
		}

		return players;

	}
	public static void savePlayerScores(String player, Integer score) throws IOException { // exception handling sollte hier nicht im service sondern auf controller ebene stattfinden

		try {
			saveScore(player, score);
		} catch (Exception e) { // Spieler gibt es noch nicht
			createPlayer(player);
			saveScore(player, score);
		}


	}
	public static void resetSaveFile() {

		try {
			createSave();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "The savefile \"save.xls\" can't be opened. Please close the file and try again.");
		}

	}
	public static void checkSave() {

		if(!saveFile.isFile()) {
			try {
				createSave();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "The savefile \"save.xls\" can't be opened. Please close the file and try again.");
			}
		}

	}

}
