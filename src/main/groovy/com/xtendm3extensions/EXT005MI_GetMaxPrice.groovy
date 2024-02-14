// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-02-09
// @version   1.0 
//
// Description 
// This API is to get a max price record from EXTMAX
// Transaction GetMaxPrice
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTYP - Contract Type
 * @param: RTPC - Rate Type
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: CTYP - Contract Type
 * @return: RTPC - Rate Type
 * @return: AMNT - Amount
 * 
*/


public class GetMaxPrice extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inCTYP
  String inRTPC
  
  // Constructor 
  public GetMaxPrice(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Contract Type
     if (mi.in.get("CTYP") != null) {
        inCTYP = mi.in.get("CTYP") 
     } else {
        inCTYP = 0       
     }
     
     // Rate Type
     if (mi.in.get("RTPC") != null) {
        inRTPC = mi.inData.get("RTPC").trim() 
     } else {
        inRTPC = ""        
     }

     // Get record
     getRecord()
  }
  
   
 //******************************************************************** 
 //Get EXTMAX record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTMAX").index("00").selectAllFields().build()
     DBContainer EXTMAX = action.getContainer()
      
     // Key value for read
     EXTMAX.set("EXCONO", inCONO)
     EXTMAX.set("EXDIVI", inDIVI)
     EXTMAX.set("EXCTYP", inCTYP)
     EXTMAX.set("EXRTPC", inRTPC)
     
    // Read  
    if (action.read(EXTMAX)) {             
      // Send output value  
      mi.outData.put("CONO", EXTMAX.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTMAX.getString("EXDIVI"))
      mi.outData.put("CTYP", EXTMAX.getInt("EXCTYP").toString())
      mi.outData.put("RTPC", EXTMAX.getString("EXRTPC"))
      mi.outData.put("AMNT", EXTMAX.getDouble("EXAMNT").toString())
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
  
}