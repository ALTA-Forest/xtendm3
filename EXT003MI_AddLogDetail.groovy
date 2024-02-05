// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add log details to EXTSLD
// Transaction AddLogDetail
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: LGID - Log ID
 * @param: GRAD - Grade
 * @param: LLEN - Length
 * @param: LEND - Length Deduction
 * @param: LSDI - Small Diameter
 * @param: LLDI - Large Diameter
 * @param: DIAD - Diameter Deduction
 * @param: LGRV - Gross Volume
 * @param: LNEV - Net Volume
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: LDID - Log Detail ID
 * 
**/


public class AddLogDetail extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inLDID
  int outLDID
  boolean numberFound
  Integer lastNumber
  String nextNumber

  // Constructor 
  public AddLogDetail(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger
     this.utility = utility
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

    // Scale Ticket ID
     int inSTID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0         
     }
     
     // Log ID
     int inLGID
     if (mi.in.get("LGID") != null) {
        inLGID = mi.in.get("LGID") 
     } else {
        inLGID = 0         
     }

     // Grade
     String inGRAD  
     if (mi.in.get("GRAD") != null) {
        inGRAD = mi.in.get("GRAD") 
     } else {
        inGRAD = ""        
     }
           
     // Length
     double inLLEN 
     if (mi.in.get("LLEN") != null) {
        inLLEN = mi.in.get("LLEN") 
     } else {
        inLLEN = 0d        
     }
 
      // Length Deduction
     double inLEND 
     if (mi.in.get("LEND") != null) {
        inLEND = mi.in.get("LEND") 
     } else {
        inLEND = 0d        
     }
     
     // Small Diameter
     double inLSDI  
     if (mi.in.get("LSDI") != null) {
        inLSDI = mi.in.get("LSDI") 
     } else {
        inLSDI = 0d       
     }

     // Large Diameter
     double inLLDI
     if (mi.in.get("LLDI") != null) {
        inLLDI = mi.in.get("LLDI") 
     } else {
        inLLDI = 0d        
     }

     // Diameter Deduction
     double inDIAD
     if (mi.in.get("DIAD") != null) {
        inDIAD = mi.in.get("DIAD") 
     } else {
        inDIAD = 0d       
     }

     // Gross Volume
     double inLGRV
     if (mi.in.get("LGRV") != null) {
        inLGRV = mi.in.get("LGRV") 
     } else {
        inLGRV = 0d       
     }

     // Net Volume
     double inLNEV
     if (mi.in.get("LNEV") != null) {
        inLNEV = mi.in.get("LNEV") 
     } else {
        inLNEV = 0d       
     }

     //Get next number 
     getNextNumber("", "L8", "1") 
     outLDID = nextNumber as Integer
     inLDID = outLDID as Integer

     // Write record 
     addEXTSLDRecord(inCONO, inDIVI, inSTID,inLGID, inLDID, inGRAD, inLLEN, inLEND, inLSDI, inLLDI, inDIAD, inLGRV, inLNEV)          

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("LDID", String.valueOf(inLDID)) 
     mi.write()
     
  }


   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTSLD")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTSLD")
     .index("20")
     .matching(expression)
     .selection("EXLDID")
     .reverse()
     .build()

     DBContainer line = actionline.getContainer() 
     
     line.set("EXCONO", inCONO)     
     
     actionline.readAll(line, 1, releasedLineProcessor)   
   
   } 

    
  //******************************************************************** 
  // List Last Number
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line -> 

      // Output
      if (!numberFound) {
        lastNumber = line.get("EXLDID") 
        numberFound = true
      }

  }

   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        Map<String, String> params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        Closure<?> callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 

  

  //******************************************************************** 
  // Add EXTSLD record 
  //********************************************************************     
  void addEXTSLDRecord(int CONO, String DIVI, int STID, int LGID, int LDID, String GRAD, double LLEN, double LEND, double LSDI, double LLDI, double DIAD, double LGRV, double LNEV){  
       DBAction action = database.table("EXTSLD").index("00").build()
       DBContainer EXTSLD = action.createContainer()
       EXTSLD.set("EXCONO", CONO)
       EXTSLD.set("EXDIVI", DIVI)
       EXTSLD.set("EXSTID", STID)
       EXTSLD.set("EXLDID", LDID)
       EXTSLD.set("EXLGID", LGID)
       EXTSLD.set("EXGRAD", GRAD)
       EXTSLD.set("EXLLEN", LLEN)
       EXTSLD.set("EXLEND", LEND)
       EXTSLD.set("EXLSDI", LSDI)
       EXTSLD.set("EXLLDI", LLDI)
       EXTSLD.set("EXDIAD", DIAD)   
       EXTSLD.set("EXLGRV", LGRV)       
       EXTSLD.set("EXLNEV", LNEV)       
       EXTSLD.set("EXCHID", program.getUser())
       EXTSLD.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTSLD.set("EXRGDT", regdate) 
       EXTSLD.set("EXLMDT", regdate) 
       EXTSLD.set("EXRGTM", regtime)
       action.insert(EXTSLD)         
 } 

     
} 

