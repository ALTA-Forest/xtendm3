// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get sorts from EXTSOR
// Transaction GetSorts
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SORT - Sort Code
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: SORT - Exception Code
 * @return: SONA - Name
 * @return: ITNO - Item Number
 * 
*/


public class GetSort extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inSORT
  
  // Constructor 
  public GetSort(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database  
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer

     // Sort Code
     if (mi.in.get("SORT") != null && mi.in.get("SORT") != "") {
        inSORT = mi.inData.get("SORT").trim() 
     } else {
        inSORT = ""         
     }
    
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTSOR record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTSOR").index("00").selectAllFields().build()
     DBContainer EXTSOR = action.getContainer()
     EXTSOR.set("EXCONO", inCONO)
     EXTSOR.set("EXSORT", inSORT)
     
    // Read  
    if (action.read(EXTSOR)) {  
      mi.outData.put("CONO", EXTSOR.get("EXCONO").toString())
      mi.outData.put("SORT", EXTSOR.getString("EXSORT"))
      mi.outData.put("SONA", EXTSOR.getString("EXSONA"))
      mi.outData.put("ITNO", EXTSOR.getString("EXITNO"))
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}