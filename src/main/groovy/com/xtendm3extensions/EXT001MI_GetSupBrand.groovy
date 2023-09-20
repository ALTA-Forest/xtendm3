// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a supplier brand from EXTSBR
// Transaction GetSupBrand
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: BRND - Brand
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: SUNO - Supplier
 * @return: BRND - Brand
 * 
*/


public class GetSupBrand extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inSUNO
  String inBRND
  
  // Constructor 
  public GetSupBrand(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
 //Get EXTSBR record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTSBR").index("00").selectAllFields().build()
     DBContainer EXTSBR = action.getContainer()
     EXTSBR.set("EXCONO", inCONO)
     EXTSBR.set("EXSUNO", inSUNO)
     EXTSBR.set("EXBRND", inBRND)
     
    // Read  
    if (action.read(EXTSBR)) {  
      mi.outData.put("CONO", EXTSBR.get("EXCONO").toString())
      mi.outData.put("SUNO", EXTSBR.getString("EXSUNO"))
      mi.outData.put("BRND", EXTSBR.getString("EXBRND"))
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}