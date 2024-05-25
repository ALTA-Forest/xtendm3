// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add a scale ticket to EXTDST
// Transaction AddScaleTicket
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STNO - Scale Ticket Number
 * @param: STDT - Scale Date
 * @param: STLR - Log Rule
 * @param: STLN - Scale Location Number
 * @param: STSN - Scaler Number
 * @param: STLP - Log Percentage
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: STID - Scale Ticket ID
 * 
**/


public class AddScaleTicket extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inSTID
  boolean numberFound
  Integer lastNumber

  // Constructor 
  public AddScaleTicket(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Delivery Number
     int inDLNO
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0         
     }

     // Scale Ticket Number
     String inSTNO  
     if (mi.in.get("STNO") != null && mi.in.get("STNO") != "") {
        inSTNO = mi.inData.get("STNO").trim() 
     } else {
        inSTNO = ""        
     }
           
     // Scale Date
     int inSTDT 
     if (mi.in.get("STDT") != null) {
        inSTDT = mi.in.get("STDT") 
        
        //Validate date format
        boolean validSTDT = utility.call("DateUtil", "isDateValid", String.valueOf(inSTDT), "yyyyMMdd")  
        if (!validSTDT) {
           mi.error("Scale Date is not valid")   
           return  
        } 

     } else {
        inSTDT = 0        
     }
 
      // Log Rule
     int inSTLR 
     if (mi.in.get("STLR") != null) {
        inSTLR = mi.in.get("STLR") 
     } else {
        inSTLR = 0        
     }
     
     // Scale Location Number
     String inSTLN  
     if (mi.in.get("STLN") != null && mi.in.get("STLN") != "") {
        inSTLN = mi.inData.get("STLN").trim() 
     } else {
        inSTLN = ""        
     }

     // Scaler Number
     String inSTSN
     if (mi.in.get("STSN") != null && mi.in.get("STSN") != "") {
        inSTSN = mi.inData.get("STSN").trim() 
     } else {
        inSTSN = ""        
     }

     // Log Percentage
     double inSTLP
     if (mi.in.get("STLP") != null) {
        inSTLP = mi.in.get("STLP") 
     } else {
        inSTLP = 0d       
     }

     // Validate Scale Ticket Line record
     Optional<DBContainer> EXTDST = findEXTDST(inCONO, inDIVI, inDLNO, inSTNO)
     if(EXTDST.isPresent()){
        mi.error("Scale Ticket already exist")   
        return             
     } else {
        findLastNumber()
        inSTID = lastNumber + 1
        // Write record 
        addEXTDSTRecord(inCONO, inDIVI, inDLNO, inSTNO, inSTDT, inSTLR, inSTLN, inSTSN, inSTID, inSTLP)          
     }  

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("STID", String.valueOf(inSTID)) 
     mi.write()
  }


   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTDST")
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTDST")
     .index("10")
     .matching(expression)
     .selection("EXSTID")
     .reverse()
     .build()

     DBContainer line = actionline.getContainer() 
     
     line.set("EXCONO", inCONO)  
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()                 
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)   
   } 

    
  //******************************************************************** 
  // List Last Number
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line -> 
      // Output
      if (!lastNumber) {
        lastNumber = line.get("EXSTID") 
        numberFound = true
      }
  }
  
  //******************************************************************** 
  // Get EXTDST record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDST(int CONO, String DIVI, int DLNO, String STNO){  
     DBAction query = database.table("EXTDST").index("00").build()
     DBContainer EXTDST = query.getContainer()
     EXTDST.set("EXCONO", CONO)
     EXTDST.set("EXDIVI", DIVI)
     EXTDST.set("EXDLNO", DLNO)
     EXTDST.set("EXSTNO", STNO)
     if(query.read(EXTDST))  { 
       return Optional.of(EXTDST)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTDST record 
  //********************************************************************     
  void addEXTDSTRecord(int CONO, String DIVI, int DLNO, String STNO, int STDT, int STLR, String STLN, String STSN, int STID, double STLP){  
       DBAction action = database.table("EXTDST").index("00").build()
       DBContainer EXTDST = action.createContainer()
       EXTDST.set("EXCONO", CONO)
       EXTDST.set("EXDIVI", DIVI)
       EXTDST.set("EXDLNO", DLNO)
       EXTDST.set("EXSTNO", STNO)
       EXTDST.set("EXSTDT", STDT)
       EXTDST.set("EXSTLR", STLR)
       EXTDST.set("EXSTLN", STLN)
       EXTDST.set("EXSTSN", STSN)
       EXTDST.set("EXSTID", STID)
       EXTDST.set("EXSTLP", STLP)       
       EXTDST.set("EXCHID", program.getUser())
       EXTDST.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDST.set("EXRGDT", regdate) 
       EXTDST.set("EXLMDT", regdate) 
       EXTDST.set("EXRGTM", regtime)
       action.insert(EXTDST)         
 } 
     
} 

