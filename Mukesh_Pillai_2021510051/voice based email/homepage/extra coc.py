flag = True
        while (flag):
            text1 = "Welcome to our Voice Based Email. Login with your email account in order to continue. "
            texttospeech(text1, file + i)
            i = i + str(1)
            texttospeech("Enter your Email", file + i)
            i = i + str(1)
            addr = speechtotext(10)

            if addr != 'N':
                texttospeech("You meant " + addr + " say yes to confirm or no to enter again", file + i)
                i = i + str(1)
                say = speechtotext(3)
                if say == 'yes' or say == 'Yes':
                    flag = False
            else:
                texttospeech("could not understand what you meant:", file + i)
                i = i + str(1)
        addr = addr.strip()
        addr = addr.replace(' ', '')
        addr = addr.lower()
        addr = convert_special_char(addr)
        print(addr)
        request.email = addr

        flag = True
        passwrd1=''
        while (flag):
            texttospeech("Enter your password's first character:", file + i)
            i = i + str(1)
            p1 = speechtotext(8)
            print(p1)
            passwrd1=passwrd1+p1
            texttospeech("Enter your password's second character:", file + i)
            i = i + str(1)
            p2 = speechtotext(8)
            print(p1)
            passwrd1=passwrd1+p2
            texttospeech("Enter your password's third character:", file + i)
            i = i + str(1)
            p3 = speechtotext(8)
            print(p3)
            passwrd1=passwrd1+p3
            texttospeech("Enter your password's fourth character:", file + i)
            i = i + str(1)
            p4 = speechtotext(8)
            print(p4)
            passwrd1=passwrd1+p4



            passwrd=passwrd1

            if addr != 'N':
                texttospeech("You meant " + passwrd + " say yes to confirm or no to enter again", file + i)
                i = i + str(1)
                say = speechtotext(3)
                if say == 'yes' or say == 'Yes':
                    flag = False
            else:
                texttospeech("could not understand what you meant:", file + i)
                i = i + str(1)
        passwrd = passwrd.strip()
        passwrd = passwrd.replace(' ', '')
        passwrd = passwrd.lower()
        passwrd = convert_special_char(passwrd)
        print(passwrd)
        request.password=passwrd