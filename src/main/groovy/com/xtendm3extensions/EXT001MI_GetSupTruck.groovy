// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a supplier truck from EXTSTR
// Transaction GetSupTruck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * 
*/

/**
 * OUT
 * @param: CONO - Company Number
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * @param: TRNA - Name
 * 
*/


public class GetSupTruck extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inSUNO
  String inTRCK
  
  // Constructor 
  public GetSupTruck(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTSTR record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTSTR").index("00").selectAllFields().build()
     DBContainer EXTSTR = action.getContainer()
     EXTSTR.set("EXCONO", inCONO)
     EXTSTR.set("EXSUNO", inSUNO)
     EXTSTR.set("EXTRCK", inTRCK)
     
    // Read  
    if (action.read(EXTSTR)) {  
      mi.outData.put("CONO", EXTSTR.get("EXCONO").toString())
      mi.outData.put("SUNO", EXTSTR.getString("EXSUNO"))
      mi.outData.put("TRCK", EXTSTR.getString("EXTRCK"))
      mi.outData.put("TRNA", EXTSTR.getString("EXTRNA"))
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}