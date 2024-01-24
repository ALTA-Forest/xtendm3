// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a contract brand from EXTCTB
// Transaction GetContrBrand
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: RVID - Revision ID
 * @param: BRND - Brand
 * 
*/

/**
 * OUT
 * @return: RVID - Revision ID
 * @return: BRND - Brand
 * @return: CBID - Brand ID
 * 
*/



public class GetContrBrand extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inDIVI
  String inRVID
  String inBRND
  
  // Constructor 
  public GetContrBrand(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""         
     }
     
     // Brand
     if (mi.in.get("BRND") != null) {
        inBRND = mi.in.get("BRND") 
     } else {
        inBRND = ""         
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTCTB record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTCTB").index("00").selectAllFields().build()
     DBContainer EXTCTB = action.getContainer()
      
     // Key value for read
     EXTCTB.set("EXCONO", inCONO)
     EXTCTB.set("EXDIVI", inDIVI)
     EXTCTB.set("EXRVID", inRVID)
     EXTCTB.set("EXBRND", inBRND)
     
    // Read  
    if (action.read(EXTCTB)) {       
      // Send output value  
      mi.outData.put("CONO", EXTCTB.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTCTB.getString("EXDIVI"))
      mi.outData.put("RVID", EXTCTB.getString("EXRVID"))
      mi.outData.put("BRND", EXTCTB.getString("EXBRND"))
      mi.outData.put("CBID", EXTCTB.getString("EXCBID"))
      mi.write()
    } else {
      mi.error("No record found")   
      return 
    }
  } 
  
}