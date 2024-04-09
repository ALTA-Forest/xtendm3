// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get species from EXTSPE
// Transaction GetSpecies
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CATE - Category
 * @param: SPEC - Species
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: SPEC - Species
 * @return: CATE - Category
 * @return: SPNA - Name
 * @return: SORT - Sort Code
 * 
*/


public class GetSpecies extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  int inCONO
  String inCATE
  String inSPEC
  
  // Constructor 
  public GetSpecies(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database  
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer

     // Category
     if (mi.in.get("CATE") != null && mi.in.get("CATE") != "") {
        inCATE = mi.inData.get("CATE").trim() 
     } else {
        inCATE = ""         
     }

     // Species
     if (mi.in.get("SPEC") != null && mi.in.get("SPEC") != "") {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""         
     }
     
     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTSPE record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTSPE").index("00").selectAllFields().build()
     DBContainer EXTSPE = action.getContainer()
     EXTSPE.set("EXCONO", inCONO)
     EXTSPE.set("EXCATE", inCATE)
     EXTSPE.set("EXSPEC", inSPEC)
     
    // Read  
    if (action.read(EXTSPE)) {  
      mi.outData.put("CONO", EXTSPE.get("EXCONO").toString())
      mi.outData.put("CATE", EXTSPE.getString("EXCATE"))
      mi.outData.put("SPEC", EXTSPE.getString("EXSPEC"))      
      mi.outData.put("SPNA", EXTSPE.getString("EXSPNA"))
      mi.outData.put("SORT", EXTSPE.getString("EXSORT"))
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
}