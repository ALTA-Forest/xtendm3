// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get an deck type from EXTDPT
// Transaction GetDeckType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: TYPE - Deck Type
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: TYPE - Deck Type
 * @return: NAME - Name
 * 
*/



public class GetDeckType extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inTYPE
  
  // Constructor 
  public GetDeckType(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database  
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Deck Type
     if (mi.in.get("TYPE") != null) {
        inTYPE = mi.in.get("TYPE") 
     } else {
        inTYPE = ""         
     }
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTDPT record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDPT").index("00").selectAllFields().build()
     DBContainer EXTDPT = action.getContainer()
     EXTDPT.set("EXCONO", inCONO)
     EXTDPT.set("EXDIVI", inDIVI)
     EXTDPT.set("EXTYPE", inTYPE)
     
    // Read  
    if (action.read(EXTDPT)) {       
      mi.outData.put("CONO", EXTDPT.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTDPT.getString("EXDIVI"))
      mi.outData.put("TYPE", EXTDPT.getString("EXTYPE")) 
      mi.outData.put("NAME", EXTDPT.getString("EXNAME")) 
      mi.write() 
    } else {
      mi.error("No record found")   
      return 
    }
  } 
  
}