// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a supplier truck weight history from EXTTWH
// Transaction GetSupTrckWtHis
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: SUNO - Supplier
 * @return: TRCK - Truck
 * @return: TRNA - Name
 * 
*/


public class GetSupTrckWtHis extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inSUNO
  String inTRCK
  int inFRDT
  int inTODT
  
  // Constructor 
  public GetSupTrckWtHis(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database  
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer

     // Supplier
     if (mi.in.get("SUNO") != null) {
        inSUNO = mi.in.get("SUNO") 
     } else {
        inSUNO = ""         
     }
     
     // Truck
     if (mi.in.get("TRCK") != null) {
        inTRCK = mi.in.get("TRCK") 
     } else {
        inTRCK = ""         
     }
     
     // From Date
     if (mi.in.get("FRDT") != null) {
        inFRDT = mi.in.get("FRDT") 
     } else {
        inFRDT = 0        
     }

     // To Date
     if (mi.in.get("TODT") != null) {
        inTODT = mi.in.get("TODT") 
     } else {
        inTODT = 0        
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTTWH record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTTWH").index("00").selectAllFields().build()
     DBContainer EXTTWH = action.getContainer()
     EXTTWH.set("EXCONO", inCONO)
     EXTTWH.set("EXSUNO", inSUNO)
     EXTTWH.set("EXTRCK", inTRCK)
     EXTTWH.set("EXFRDT", inFRDT)
     EXTTWH.set("EXTODT", inTODT)
     
    // Read  
    if (action.read(EXTTWH)) {  
      mi.outData.put("CONO", EXTTWH.get("EXCONO").toString())
      mi.outData.put("SUNO", EXTTWH.getString("EXSUNO"))
      mi.outData.put("TRCK", EXTTWH.getString("EXTRCK"))
      mi.outData.put("TARE", EXTTWH.get("EXTARE").toString())
      mi.outData.put("FRDT", EXTTWH.get("EXFRDT").toString())
      mi.outData.put("TODT", EXTTWH.get("EXTODT").toString())
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}